package fetcher.handler

import fetcher.{MsgHandlerConfig, StringDB}
import org.json4s.jackson.Serialization.{write => toJsonStr}

import scala.xml.NodeSeq

case class GroupsHandler(db: StringDB, conf: MsgHandlerConfig)
  extends MessageHandler[String]
  with Publish[String]
  with Unpublish[String]
  with GroupParser
  with GenerateIdForEachContent {}

trait GroupParser extends StringContentParser {
  override def parse(xml: NodeSeq) = {
    val groupXml = xml \\ "group"
    toJsonStr(
      Group(
        groupXml \ "group_pid" text,
        groupXml \ "group_name" text,
        groupXml \ "standard_image" text,
        groupXml \ "vertical_image" text,
        groupXml \ "channel" text,
        groupXml \ "editorial_label" text,
        (groupXml \ "series_stacked" text).toBoolean,
        (groupXml \\ "related_links" map { linkNode =>
          Link(linkNode \ "text" text, linkNode \ "link" text)
        }).toList
      ))
  }
}


case class Link(title: String, url: String)
case class Group(pid: String, name: String, standardImage: String, verticalImage: String,
                 channel: String, editorialLabel: String, seriesStacked: Boolean, relatedLinks: List[Link])
