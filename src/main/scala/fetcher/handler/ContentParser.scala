package fetcher.handler

import scala.xml.NodeSeq

trait ContentParser[A] {
  def parse(xml: NodeSeq): A
}

trait SetContentParser extends ContentParser[Set[String]] {
  override def parse(xml: NodeSeq) = (xml \\ "pid" map (_.text)).toSet
}

trait StringContentParser extends ContentParser[String] {
  implicit val formats = org.json4s.DefaultFormats
  override def parse(xml: NodeSeq): String
}



