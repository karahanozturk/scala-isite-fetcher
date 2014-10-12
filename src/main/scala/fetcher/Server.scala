package fetcher

import akka.actor.ActorSystem
import com.amazonaws.ClientConfiguration
import com.amazonaws.services.sqs.AmazonSQSClient
import com.redis.RedisClient
import com.timgroup.statsd.NonBlockingStatsDClient
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import dispatch.Http
import fetcher.handler.{BlacklistHandler, EditorialLabelsHandler, GroupsHandler, RRCHandler}
import org.slf4j.LoggerFactory
import org.xml.sax.SAXParseException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Server {

  val conf = ConfigFactory.load()
  val logger = Logger(LoggerFactory.getLogger("iSite Fetcher"))
  val statsConf = conf.getConfig("stats")
  val statsD = new NonBlockingStatsDClient(statsConf.getString("prefix"), statsConf.getString("host"), statsConf.getInt("port"))

  def main(args: Array[String]) = {
    val controller = createController
    val scheduler = ActorSystem().scheduler

    def startFetcher() {
      scheduler.scheduleOnce(5 seconds) {
        val process = controller startPolling()

        process onComplete {
          case Success(_) => informSuccess()
          case Failure(t) => t match {
            case t: FetcherException => informError(t.tag, t.getMessage)
            case t: SAXParseException => informError("parsing_errors", t.getMessage)
            case _ => informError("processing_failures", t.getMessage)
          }
        }

        process onComplete (_ => startFetcher())
      }
    }

    startServer
    startFetcher()
  }

  def createController = {
    val sqsConfig = new ClientConfiguration()
    val sqs = new AmazonSQSClient(sqsConfig)
    val qConf = conf.getConfig("queue")
    val queueConfig = new QueueConfig(qConf.getString("url"), qConf.getInt("waitTimeSeconds"), qConf.getInt("maxNumberOfMessages"), qConf.getInt("pollingDelay"))
    val queue = new Queue(sqs, queueConfig)

    val cache = new RedisClient(conf.getString("cacheHost"), 6379)
    val setDB = SetDB(cache)
    val stringDB = StringDB(cache)

    val blacklistConf = conf.getConfig("blackList")
    val labelsConf = conf.getConfig("editorialLabels")
    val rrcConf = conf.getConfig("rrc")
    val groupConf = conf.getConfig("groups")

    val msgHandlers =
        BlacklistHandler(setDB, MsgHandlerConfig(blacklistConf.getString("msgType"), blacklistConf.getString("cacheKeyPrefix"))) ::
        EditorialLabelsHandler(setDB, MsgHandlerConfig(labelsConf.getString("msgType"), labelsConf.getString("cacheKeyPrefix"))) ::
        RRCHandler(stringDB, MsgHandlerConfig(rrcConf.getString("msgType"), rrcConf.getString("cacheKeyPrefix"))) ::
        GroupsHandler(stringDB, MsgHandlerConfig(groupConf.getString("msgType"), groupConf.getString("cacheKeyPrefix"))) :: Nil

    val isiteConf = conf.getConfig("isite")
    val client = new ISiteClient(ISiteConfig(isiteConf.getString("baseUrl"), isiteConf.getString("apiKey")), Http)

    new Controller(queue, msgHandlers, client)
  }

  def startServer = Future {
    import java.io._
    import java.net._

    val server = new ServerSocket(8080)
    while (true) {
      val s = server.accept()
      val out = new PrintStream(s.getOutputStream)

      out.println("OK")
      out.flush()
      s.close()
    }
  }

  def informSuccess() = {
    logger.info("Message successfully processed")
    statsD.increment("messages_processed")
  }

  def informError(tag: String, errorMsg: String) = {
    logger.info("Failed to processMessage processed")
    statsD.increment("processing_failures")
  }

}
