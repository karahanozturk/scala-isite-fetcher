package fetcher

import com.amazonaws.services.sqs.model.Message
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Future
import scala.util.Try

class ApplicationTest extends Specification with Mockito {

  val queue = mock[Queue]
  val msgHandler = mock[MessageHandler]
  val parser = mock[ContentParser]
  val pollDelay = 0

  val msg = new Message().withReceiptHandle("receiptHandle").withBody("body")
  val notification = Notification("type", "contentId", "fileId")
  queue.pollMessage() returns Future.successful(msg)
  msgHandler.canHandle("contentId") returns true
  msgHandler.handle(notification) returns Future.successful(Try())
  parser.extractNotification(msg) returns notification

  val app = new Application(queue, List(msgHandler), parser, pollDelay)

  "Fetcher Controller" should {
    app.startPolling()
    "delegate received messages to to the message handler" in {
      there was atLeastOne(msgHandler).handle(notification)
    }

    "and remove the message from the queue" in {
      there was atLeastOne(queue).deleteMessage(msg)
    }
  }

}
