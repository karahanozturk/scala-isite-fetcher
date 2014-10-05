package fetcher

import akka.actor.ActorSystem
import com.timgroup.statsd.StatsDClient
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Server {
  val statsD: StatsDClient = null
  val controller: Controller = null

  val logger = Logger(LoggerFactory.getLogger("iSite Fetcher"))
  val scheduler = ActorSystem().scheduler

  def start() {
    scheduler.scheduleOnce(5 seconds) {
      val process = controller startPolling()

      process onComplete {
        case Success(_) => informSuccess()
        case Failure(t) => t match {
          case t: FetcherException => informError(t.tag, t.getMessage)
          case _ => informError("processing_failures", t.getMessage)
        }
      }

      process onComplete (_ => start())
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
