package fetcher

import com.redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait MessageHandler {
  def canHandle(contentId: String): Boolean
  def handle(notification: Notification): Future[Try[Unit]]
}

class PidListMsgHandler(cache: RedisClient, client: ISiteClient, parser: ContentParser, conf: PidListHandlerConfig) extends MessageHandler {

  def saveIntoCache(pids: List[String]) = {
    cache.pipeline { p =>
      p.del(conf.cacheKey)
      p.sadd(conf.cacheKey, pids)
    }
  }

  override def canHandle(contentId: String) = contentId == conf.contentId

  override def handle(notification: Notification) = Future {
    notification.contentId match {
      case "publish" => Try(
        client.get(conf.contentId) map {
          case Response(200, xmlBody) => saveIntoCache(parser.parsePids(xmlBody))
          case _ => throw new RuntimeException("Request to iSite failed")
        }
      )
    }
  }
}