package fetcher

import com.ning.http.client.Response
import dispatch.{Http, Req}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.xml.XML

class ISiteClientTest extends Specification with Mockito {

  val http = mock[Http]

  "ISiteCLient" should {
    "fetch response from the iSite" in {
      val contentId = "guid"
      val conf = ISiteConfig("baseUrl", "apiKey")
      val httpResponse = mock[Response]
      httpResponse.getStatusCode returns 200
      val expectedXmlResponse = Source.fromURL(getClass.getResource("/isiteResponse.xml")).mkString
      httpResponse.getResponseBody returns expectedXmlResponse
      http(any[Req])(any[ExecutionContext]) returns Future.successful(httpResponse)

      val client = new ISiteClient(conf, http)
      val response = Await.result(client get contentId, scala.concurrent.duration.DurationInt(1000).millis)

      response.status must equalTo(200)
      response.contentType must equalTo("contentType")
      response.fileId must equalTo("fileId")
      response.xml \\ "document" must equalTo(XML.loadString(expectedXmlResponse) \\ "document")
    }
  }

}
