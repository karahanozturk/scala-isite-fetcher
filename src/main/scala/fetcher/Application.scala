package fetcher

import akka.actor.ActorSystem
import com.timgroup.statsd.StatsDClient
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


case class Notification(typ: String, projectId: String, contentId: String)

class Application(queue: Queue, messageHandlers: List[MessageHandler], statsD: StatsDClient) {
  val logger = Logger(LoggerFactory.getLogger("iSite Fetcher"))
  val scheduler = ActorSystem().scheduler

  def delegateMessage(msg: Message) = {
      val availableHandlers = messageHandlers.filter(_.canHandle(msg.notification.contentId))

      availableHandlers.size match {
        case 0 => informError("No handler found")
        case 1 =>
          val handler = availableHandlers.head
          handler.handle(msg.notification) map {
            case Success(_) => deleteMsg(msg)
            case Failure(t) => informError(t.getMessage)
          }
        case _ => informError("Too many handlers found for the message")
      }
  }

  def deleteMsg(msg: Message) = {
    queue.deleteMessage(msg) onComplete {
    case Success(_) => informSuccess(msg)
    case Failure(t) => informError(t.getMessage)
  }}

  def startPolling() {
    queue pollMessage() onComplete {
      case Success(msg) =>
        msg.notification match {
          case Notification(_, "iplayer", _) => delegateMessage(msg)
          case _ => deleteMsg(msg)
        }
      case Failure(t) =>
        informError(t.getMessage)
    }
  }

  def informSuccess(msg: Message) = {
    logger.info("Message successfully processed" + msg)
    statsD.increment("messages_processed")
  }

  def informError(errorMsg: String) = {
    logger.info("Failed to processMessage processed")
    statsD.increment("processing_failures")
  }
}
