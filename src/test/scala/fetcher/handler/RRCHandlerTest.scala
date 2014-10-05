package fetcher.handler

import org.specs2.mutable.Specification

import scala.io.Source
import scala.xml.XML
import org.json4s.jackson.Serialization.{write => toJsonStr}

class RRCHandlerTest extends Specification {
  implicit val formats = org.json4s.DefaultFormats

  "RRC Parser" should {
    "parse XML into RRC json string" in {
      val xml = XML.loadString(Source.fromURL(getClass.getResource("/isiteRrcResponse.xml")).mkString)
      val parser = new TestRRCParser
      val rrc = RRC("versionPid", "Short description", "Long description", "http://url")

      parser.parse(xml) must equalTo(toJsonStr(rrc))
    }
  }
}

class TestRRCParser extends RRCParser
