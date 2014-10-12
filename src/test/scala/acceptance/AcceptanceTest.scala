// package acceptance

// import java.util

// import _root_.util.MockServer
// import com.amazonaws.services.sqs.model.{ReceiveMessageRequest, ReceiveMessageResult}
// import com.amazonaws.services.sqs.{AmazonSQSClient, model}
// import com.github.tomakehurst.wiremock.client.WireMock._
// import com.redis.RedisClient
// import com.typesafe.config.ConfigFactory
// import dispatch.Http
// import fetcher._
// import fetcher.handler._
// import org.specs2.mock.Mockito
// import org.specs2.mutable.Specification

// import scala.concurrent.Await
// import scala.io.Source

// class AcceptanceTest extends Specification with Mockito {
//   isolated

//   val conf = ConfigFactory.load()

//   "iBL iSite Fetcher" >> {

//     val sqs = mock[AmazonSQSClient]
//     val qConf = conf.getConfig("queue")
//     val queueConfig = new QueueConfig(qConf.getString("url"), qConf.getInt("waitTimeSeconds"), qConf.getInt("maxNumberOfMessages"), qConf.getInt("pollingDelay"))
//     val queue = new Queue(sqs, queueConfig)

//     val client = new ISiteClient(ISiteConfig("http://localhost:3333/isite2-content-reader", "apiKey"), Http)

//     val cache = new RedisClient(conf.getString("cacheHost"), 6379)
//     val setDB = SetDB(cache)
//     val stringDB = StringDB(cache)

//     val blacklistConf = conf.getConfig("blackList")
//     val labelsConf = conf.getConfig("editorialLabels")
//     val rrcConf = conf.getConfig("rrc")
//     val groupConf = conf.getConfig("groups")

//     val msgHandlers =
//       BlacklistHandler(setDB, MsgHandlerConfig(blacklistConf.getString("msgType"), blacklistConf.getString("cacheKeyPrefix"))) ::
//         EditorialLabelsHandler(setDB, MsgHandlerConfig(labelsConf.getString("msgType"), labelsConf.getString("cacheKeyPrefix"))) ::
//         RRCHandler(stringDB, MsgHandlerConfig(rrcConf.getString("msgType"), rrcConf.getString("cacheKeyPrefix"))) ::
//         GroupsHandler(stringDB, MsgHandlerConfig(groupConf.getString("msgType"), groupConf.getString("cacheKeyPrefix"))) :: Nil

//     val controller = new Controller(queue, msgHandlers, client)

//     MockServer.start()

//     "when an episode is added to blacklist" >> {
//       val sqsMsg = Source.fromURL(getClass.getResource("/snsNotification")).mkString
//       val sqsMessages = new util.ArrayList[Message]
//       val message = new model.Message().withReceiptHandle("receiptHandle").withBody(sqsMsg)
//       val result = new ReceiveMessageResult().withMessages(message)
//       sqs.receiveMessage(any[ReceiveMessageRequest]) returns result

//       stubFor(get(urlMatching("/isite2-content-reader.*")).
//         willReturn(aResponse().
//         withStatus(200).
//         withBody(Source.fromURL(getClass.getResource("/isiteBlacklistResponse.xml")).mkString)))

//       "the blacklist should be updated in the cache" in {
//         Await.result(controller.startPolling(), scala.concurrent.duration.DurationInt(1000).millis)
//         MockServer.stop()

//         cache.smembers("ibl:v2:blacklist").get.flatten must equalTo(Set("pid3", "pid2", "pid1"))
//       }.pendingUntilFixed("integrate in memory redis")
//     }
//   }

// }
