package fetcher

import fetcher.handler.MessageHandler
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.{Await, Future}
import scala.util.{Try, Failure, Success}

class ControllerTest extends Specification with Mockito {

  //@todo use beforeEach to reduce code
  "Fetcher Controller" should {
    "delegate received message to the message handler" in {
      val msg = Message("iplayer", "publish", "contentId", "receiptHandle")
      val queue = mock[Queue]
      queue.deleteMessage(any[Message]) returns Future.successful()
      queue.pollMessage() returns Future.successful(msg)

      val client = mock[ISiteClient]
      client.get("contentId") returns Future.successful(ISiteResponse(200, "contentType", "fileId", <xml></xml>))

      val content = ISiteContent("publish", "fileId", <xml></xml>)
      val msgHandler = mock[MessageHandler[_]]
      msgHandler.canHandle("contentType") returns true
      msgHandler.handle(content) returns Future.successful()

      val controller = new Controller(queue, List(msgHandler), client)
      Await.result(controller.startPolling(), scala.concurrent.duration.DurationInt(1000).millis)

      there was one(msgHandler).handle(content)
      there was one(queue).deleteMessage(msg)
    }

    "delete the message if project id is not iplayer" in {
      val msg = Message("otherProject", "publish", "contentId", "receiptHandle")
      val queue = mock[Queue]
      queue.pollMessage() returns Future.successful(msg)
      queue.deleteMessage(any[Message]) returns Future.successful {}
      val client = mock[ISiteClient]

      val controller = new Controller(queue, List(mock[MessageHandler[_]]), client)
      Await.result(controller.startPolling(), scala.concurrent.duration.DurationInt(1000).millis)

      there was one(queue).deleteMessage(msg)
      there was no(client).get("contentId")
    }

    "throw exception and delete the message when no available handlers found" in {
      val msg = Message("iplayer", "publish", "contentId", "receiptHandle")
      val queue = mock[Queue]
      queue.deleteMessage(any[Message]) returns Future.successful()
      queue.pollMessage() returns Future.successful(msg)

      val client = mock[ISiteClient]
      client.get("contentId") returns Future.successful(ISiteResponse(200, "contentType", "fileId", <xml></xml>))

      val msgHandler = mock[MessageHandler[_]]
      msgHandler.canHandle("contentType") returns false

      val controller = new Controller(queue, List(msgHandler), client)
      var caughtOnFailure: Throwable = null

      Try {
        val process = controller.startPolling()
        process onComplete {
          case Success(_) => assert(assertion = false, "Should not complete successfully")
          case Failure(t) => caughtOnFailure = t
        }
        Await.result(process, scala.concurrent.duration.DurationInt(1000).millis)
      }

      there was one(queue).deleteMessage(msg)
      caughtOnFailure.isInstanceOf[FetcherException] must equalTo(true)
    }

    "throw exception when client fetches unsuccessful response from iSite" in {
      val msg = Message("iplayer", "publish", "contentId", "receiptHandle")
      val queue = mock[Queue]
      queue.deleteMessage(any[Message]) returns Future.successful()
      queue.pollMessage() returns Future.successful(msg)

      val client = mock[ISiteClient]
      client.get("contentId") returns Future.successful(ISiteResponse(404, "contentType", "fileId", <xml></xml>))

      val controller = new Controller(queue, List(mock[MessageHandler[_]]), client)
      var caughtOnFailure: Throwable = null

      Try {
        val process = controller.startPolling()
        process onComplete {
          case Success(_) => assert(assertion = false, "Should not complete successfully")
          case Failure(t) => caughtOnFailure = t
        }
        Await.result(process, scala.concurrent.duration.DurationInt(1000).millis)
      }

      there was no(queue).deleteMessage(msg)
      caughtOnFailure.isInstanceOf[FetcherException] must equalTo(true)
    }
  }

}
