package fetcher

import fetcher.handler.MessageHandler

import scala.concurrent.ExecutionContext.Implicits.global
import scala.xml.NodeSeq

case class ISiteContent(publicationType: String, fileId: String, xml: NodeSeq)
class FetcherException(val tag: String, msg: String) extends RuntimeException(msg)

class Controller(queue: Queue, msgHandlers: List[MessageHandler[_]], client: ISiteClient) {

  private def delegateToHandler(msg: Message, body: Body) =
    msgHandlers.filter(_.canHandle(body contentType)) match {
      case head :: tail =>
        val content = ISiteContent(msg.publishType, body.fileId, body.xml)
        head.handle(content) flatMap (_ => queue deleteMessage msg)
      case Nil =>
        queue deleteMessage msg
        throw new FetcherException("processing_failures", "No handler found")
    }

  private def getContent(msg: Message) = client.get(msg.contentId) flatMap {
    case ISiteResponse(200, Some(body)) => delegateToHandler(msg, body)
    case ISiteResponse(status, _) => throw new FetcherException(s"isite_responses_$status", "Failed to fetch data from ISite")
  }

  def startPolling() = queue.pollMessage() flatMap {
    case msg @ Message("iplayer", _, _, _) => getContent(msg)
    case msg => queue deleteMessage msg
  }
}