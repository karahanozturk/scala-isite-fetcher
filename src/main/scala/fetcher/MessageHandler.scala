package fetcher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import scala.xml.XML

trait MessageHandler {
  def canHandle(contentId: String): Boolean
  def handle(notification: Notification): Future[Try[Unit]]
}

class PidListMsgHandler(db: DB, client: ISiteClient, conf: PidListHandlerConfig) extends MessageHandler {

  override def canHandle(contentId: String) = contentId == conf.contentId

  override def handle(notification: Notification) = Future {
    def parsePids(xml: String) = XML.loadString(xml)  \\ "pid" map (_.text)

    notification.contentId match {
      case "publish" => Try(
        client.get(conf.contentId) map {
          case Response(200, xmlBody) => db.updateSet(conf.cacheKey, parsePids(xmlBody).toSet)
          case _ => throw new RuntimeException("Request to iSite failed")
        }
      )
    }
  }
}