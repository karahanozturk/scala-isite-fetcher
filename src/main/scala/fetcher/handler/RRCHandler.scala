package fetcher.handler

import fetcher.{MsgHandlerConfig, StringDB}
import org.json4s.jackson.Serialization.{write => toJsonStr}

import scala.xml.NodeSeq

case class RRCHandler(db: StringDB, conf: MsgHandlerConfig)
  extends MessageHandler[String]
  with Publish[String]
  with Unpublish[String]
  with RRCParser
  with DBIdGeneratorForIndividual {}

trait RRCParser extends StringContentParser {
  override def parse(xml: NodeSeq) = {
    val rrcXml = xml \\ "rrc_label"
    toJsonStr(
      RRC(
        rrcXml \ "versionPid" text,
        rrcXml \ "short_description" text,
        rrcXml \ "long_description" text,
        rrcXml \ "url" text
      ))
  }
}

case class RRC(versionPid: String, shortDescription: String, longDescription: String, url: String)