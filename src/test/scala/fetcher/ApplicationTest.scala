package fetcher

import com.timgroup.statsd.StatsDClient
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Future
import scala.util.Try

class ApplicationTest extends Specification with Mockito {

  val queue = mock[Queue]
  val msgHandler = mock[MessageHandler]
  val statsD = mock[StatsDClient]

  val notification = Notification("type", "contentId", "fileId")
  val msg = new Message("receiptHandle", notification)
  queue.pollMessage() returns Future.successful(msg)
  msgHandler.canHandle("contentId") returns true thenReturns false
  msgHandler.handle(notification) returns Future.successful(Try())

  "Fetcher Controller" should {
    "delegate received messages to to the message handler" in {
      val app = new Application(queue, List(msgHandler), statsD)

      app.startPolling()
      there was one(msgHandler).handle(notification)
      "and remove the message from the queue" in {
        there was one(queue).deleteMessage(msg)
      }
    }

    "not handle if no available handlers found" in {
      val msgHandler = mock[MessageHandler]
      msgHandler.canHandle("contentId") returns false
      val app = new Application(queue, List(msgHandler), statsD)

      app.startPolling()
      there was no(msgHandler).handle(notification)
    }

    "not handle if more than one handlers found" in {
      val msgHandler1 = mock[MessageHandler]
      val msgHandler2 = mock[MessageHandler]
      msgHandler1.canHandle("contentId") returns true
      msgHandler2.canHandle("contentId") returns true
      val app = new Application(queue, List(msgHandler1, msgHandler2), statsD)

      app.startPolling()
      there was no(msgHandler1).handle(notification)
      there was no(msgHandler2).handle(notification)
    }
  }
}
