package fetcher

import fetcher.handler.MessageHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.NodeSeq

case class ISiteContent(publicationType: String, fileId: String, xml: NodeSeq)
class FetcherException(val tag: String, msg: String) extends RuntimeException(msg)

class Controller(queue: Queue, messageHandlers: List[MessageHandler[_]], client: ISiteClient) {

  private def delegateToHandler(msg: Message, response: ISiteResponse) = {
    val availableHandlers = messageHandlers.filter(_.canHandle(response.contentType))
    availableHandlers.size match {
      case 0 =>
        queue.deleteMessage(msg)
        throw new FetcherException("processing_failures", "No handler found")
      case 1 =>
        val content = ISiteContent(msg.publishType, response.fileId, response.xml)
        availableHandlers.head handle content flatMap (_ => queue.deleteMessage(msg))
    }
  }

  private def getContent(msg: Message) = client.get(msg.contentId) flatMap {
    case response@ISiteResponse(200, _, _, _) => delegateToHandler(msg, response)
    case ISiteResponse(status, _, _, _) => throw new FetcherException(s"isite_responses_$status", "Failed to fetch data from ISite")
  }

  def startPolling() = queue.pollMessage() flatMap {
    case msg@Message("iplayer", _, _, _) => getContent(msg)
    case msg => queue.deleteMessage(msg)
  }

}