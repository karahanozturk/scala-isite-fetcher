package fetcher.handler

import org.json4s.jackson.Serialization.{write => toJsonStr}
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification

import scala.io.Source
import scala.xml.XML

class GroupsHandlerTest extends Specification {
  implicit val formats = org.json4s.DefaultFormats

  "Group Parser" should {
    "parse XML into Group json string" in new GroupParser with Scope {
      val xml = XML.loadString(Source.fromURL(getClass.getResource("/isiteGroupResponse.xml")).mkString)
      val group = Group("groupPid", "London Fashion Week", "p026gxsq", "p027b5fm", "channel", "editorialLabel", seriesStacked = true,
        List(Link("link title 1", "http://www.bbc.co.uk/iplayer/groups/p0269bx6"),
          Link("link title 2", "http://www.bbc.co.uk/iplayer/groups/p0269bx7")
        ))

      parse(xml) must equalTo(toJsonStr(group))
    }
  }
}