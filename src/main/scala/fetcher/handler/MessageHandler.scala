package fetcher.handler

import fetcher.{DB, ISiteContent, MsgHandlerConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MessageHandler[A] {
  val db: DB[A]
  val conf: MsgHandlerConfig

  def publish(content: ISiteContent) = {}
  def unpublish(content: ISiteContent) = {}

  def canHandle(`type`: String) = `type` == conf.msgType

  def handle(content: ISiteContent) = Future {
    content match {
      case ISiteContent("publish", _, _) => publish(content)
      case ISiteContent("unpublish", _, _) => unpublish(content)
    }
  }
}

trait Publish[A] extends MessageHandler[A] with ContentParser[A] with DBIdGenerator {
  override def publish(content: ISiteContent) = db.save(id(content, conf), parse(content.xml))
}

trait Unpublish[A] extends MessageHandler[A] with DBIdGenerator {
  override def unpublish(content: ISiteContent) = db.remove(id(content, conf))
}
