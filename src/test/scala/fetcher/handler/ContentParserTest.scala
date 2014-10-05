package fetcher.handler

import org.specs2.mutable.Specification

import scala.io.Source
import scala.xml.XML

class ContentParserTest extends Specification {

  "SetContentParser " should {
    "extract the set of pids from xml" in {
      val xml = XML.loadString(Source.fromURL(getClass.getResource("/isiteResponse.xml")).mkString)
      val parser = new TestSetContentParser
      parser.parse(xml) must equalTo(Set("pid1", "pid2", "pid3"))
    }
  }
}

class TestSetContentParser extends SetContentParser