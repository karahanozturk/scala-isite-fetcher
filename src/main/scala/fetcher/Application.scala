package fetcher

import akka.actor.ActorSystem
import com.amazonaws.services.sqs.model.Message

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class Notification(typ: String, contentId: String, fileId: String)

class Application(queue: Queue, messageHandlers: List[MessageHandler], parser: ContentParser, pollDelay: Int) {
  val scheduler = ActorSystem().scheduler

  def delegateMessage(msg: Message) = {
    val notification = parser.extractNotification(msg)
    val availableHandlers = messageHandlers.filter(_.canHandle(notification.contentId))

    availableHandlers.size match {
      case 0 => informError("No handler found")
      case 1 =>
        val handler = availableHandlers.head
        handler.handle(notification) map {
          case Success(_) => deleteMsg(msg)
          case Failure(t) => informError(t.getMessage)
        }
      case _ => informError("Too many handlers found for the message")
    }
  }

  def deleteMsg(msg: Message) = queue.deleteMessage(msg) onComplete {
    case Success(_) => informSuccess(msg)
    case Failure(t) => informError(t.getMessage)
  }

  def startPolling() {
    scheduler.scheduleOnce(pollDelay seconds) {
      queue pollMessage() onComplete {
        case Success(msg) =>
          delegateMessage(msg)
          startPolling()
        case Failure(t) =>
          informError(t.getMessage)
          startPolling()
      }
    }
  }

  def informSuccess(msg: Message) = System.out.println("use logger and statsd")
  def informError(errorMsg: String) = System.out.println("use logger and statsd")

  startPolling()
}
