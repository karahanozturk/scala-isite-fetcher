package fetcher.handler

import fetcher.{DB, ISiteContent, MsgHandlerConfig}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.xml.NodeSeq

class MessageHandlerTest extends Specification with Mockito {

  val db = mock[DB[String]]
  val conf = MsgHandlerConfig("msgType", "xmlContentType", "cacheKeyPrefix:")

  "MessageHandler" should {
    "call publish when publish type is 'publish'" in {
      val handler = new TestMsgHandler(db, conf)
      handler handle ISiteContent("publish", "fileId", <xml></xml>)

      handler.publishCalled must equalTo(true)
      handler.unpublishCalled must equalTo(false)
    }

    "call unpublish when publish type is 'unpublish'" in {
      val handler = new TestMsgHandler(db, conf)
      handler handle ISiteContent("unpublish", "fileId", <xml></xml>)

      handler.unpublishCalled must equalTo(true)
      handler.publishCalled must equalTo(false)
    }

    "generate persistence id from the content and configuration" in {
      val handler = new TestMsgHandler(db, conf)
      handler.id(ISiteContent("unpublish", "fileId", <xml></xml>)) must equalTo("cacheKeyPrefix:fileId")
    }
  }

  "Publish Message Handler" should {
    "save the parsed content in DB" in {
      val handler = new TestPublishMessageHandler(db, conf)
      handler handle ISiteContent("publish", "fileId", <xml></xml>)

      there was one(db).save("cacheKeyPrefix:fileId", "parsedData")
    }
  }

  "Unpublish Message Handler" should {
    "remove the content from DB" in {
      val handler = new TestUnpublishMessageHandler(db, conf)
      handler handle ISiteContent("unpublish", "fileId", <xml></xml>)

      there was one(db).remove("cacheKeyPrefix:fileId")
    }
  }
}

class TestMsgHandler(val db: DB[String], val conf: MsgHandlerConfig) extends MessageHandler[String] {
  var publishCalled = false
  var unpublishCalled = false

  override def publish(content: ISiteContent) = publishCalled = true
  override def unpublish(content: ISiteContent) = unpublishCalled = true
}

class TestPublishMessageHandler(val db: DB[String], val conf: MsgHandlerConfig) extends MessageHandler[String] with Publish[String] with TestContentParser
class TestUnpublishMessageHandler(val db: DB[String], val conf: MsgHandlerConfig) extends MessageHandler[String] with Unpublish[String] with TestContentParser

trait TestContentParser extends ContentParser[String] {
  override def parse(xml: NodeSeq) = "parsedData"
}