package fetcher

import com.netaporter.uri.dsl._
import dispatch.{Http, url}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, XML}

case class ISiteResponse(status: Int, body: Option[Body])
case class Body(contentType: String, fileId: String, xml: Elem)

class ISiteClient(conf: ISiteConfig, http: Http) {

  def get(contentId: String): Future[ISiteResponse] = {
    val uri = conf.baseUrl ? ("contentId" -> contentId) & ("api_key" -> conf.apiKey) & ("allowNonLive" -> true)
    val request = url(uri.toString()).GET

    http(request).map(response => {
      val status = response.getStatusCode
      status match {
        case 200 =>
          val xml = XML.loadString(response.getResponseBody)
          ISiteResponse(status, Some(Body(xml \\ "metadata" \ "type" text, xml \\ "metadata" \ "fileId" text, xml)))
        case _ => ISiteResponse(status, None)
      }
    })
  }
}
