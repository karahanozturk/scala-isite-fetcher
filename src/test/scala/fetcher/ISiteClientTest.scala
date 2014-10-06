package fetcher

import com.ning.http.client.Response
import dispatch.{Http, Req}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.xml.XML

class ISiteClientTest extends Specification with Mockito {
  isolated

  "ISiteClient should" >> {

    val contentId = "guid"
    val conf = ISiteConfig("baseUrl", "apiKey")
    val httpResponse = mock[Response]
    httpResponse.getStatusCode returns 200
    val expectedXmlResponse = Source.fromURL(getClass.getResource("/isiteResponse.xml")).mkString
    httpResponse.getResponseBody returns expectedXmlResponse

    val http = mock[Http]
    http(any[Req])(any[ExecutionContext]) returns Future.successful(httpResponse)

    val client = new ISiteClient(conf, http)

    "return fetched data from iSite when the request is successful" >> {
      val response = Await.result(client get contentId, scala.concurrent.duration.DurationInt(1000).millis)

      response.status must equalTo(200)
      response.body.get.contentType must equalTo("contentType")
      response.body.get.fileId must equalTo("fileId")
      response.body.get.xml \\ "document" must equalTo(XML.loadString(expectedXmlResponse) \\ "document")
    }

    "return status code only when the request is not successful" >> {
      httpResponse.getStatusCode returns 404

      val response = Await.result(client get contentId, scala.concurrent.duration.DurationInt(1000).millis)

      response.status must equalTo(404)
      response.body must equalTo(None)
    }
  }

}
