package fetcher

import fetcher.handler.MessageHandler
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.{Await, Future}
import scala.util.{Try, Failure, Success}

class ControllerTest extends Specification with Mockito {
  isolated
  
  "Fetcher Controller should" >> {

    val msg = Message("iplayer", "publish", "contentId", "receiptHandle")
    val queue = mock[Queue]
    queue.deleteMessage(any[Message]) returns Future.successful()
    queue.pollMessage() returns Future.successful(msg)

    val client = mock[ISiteClient]
    client.get("contentId") returns Future.successful(ISiteResponse(200, Some(Body("contentType", "fileId", <xml></xml>))))

    val content = ISiteContent("publish", "fileId", <xml></xml>)
    val msgHandler = mock[MessageHandler[_]]
    msgHandler.canHandle("contentType") returns true
    msgHandler.handle(content) returns Future.successful()

    val controller = new Controller(queue, List(msgHandler), client)


    "delegate received message to the message handler" >> {
      Await.result(controller.startPolling(), scala.concurrent.duration.DurationInt(1000).millis)

      there was one(msgHandler).handle(content)
      there was one(queue).deleteMessage(msg)
    }

    "delete the message if project id is not iplayer" >> {
      val otherProjectMsg = Message("otherProject", "publish", "contentId", "receiptHandle")
      queue.pollMessage() returns Future.successful(otherProjectMsg)

      Await.result(controller.startPolling(), scala.concurrent.duration.DurationInt(1000).millis)

      there was one(queue).deleteMessage(otherProjectMsg)
      there was no(client).get("contentId")
    }

    "throw exception and delete the message when no available handlers found" >> {
      msgHandler.canHandle("contentType") returns false
      var caughtOnFailure: Option[Throwable] = None

      Try {
        val process = controller.startPolling()
        process onComplete {
          case Success(_) => assert(assertion = false, "Should not complete successfully")
          case Failure(t) => caughtOnFailure = Some(t)
        }
        Await.result(process, scala.concurrent.duration.DurationInt(1000).millis)
      }

      there was one(queue).deleteMessage(msg)
      caughtOnFailure.get.isInstanceOf[FetcherException] must equalTo(true)
    }

    "throw exception when client fetches unsuccessful response from iSite" >> {
      client.get("contentId") returns Future.successful(ISiteResponse(404, None))
      var caughtOnFailure: Option[Throwable] = None

      Try {
        val process = controller.startPolling()
        process onComplete {
          case Success(_) => assert(assertion = false, "Should not complete successfully")
          case Failure(t) => caughtOnFailure = Some(t)
        }
        Await.result(process, scala.concurrent.duration.DurationInt(1000).millis)
      }

      there was no(queue).deleteMessage(msg)
      caughtOnFailure.get.isInstanceOf[FetcherException] must equalTo(true)
    }
  }

}
