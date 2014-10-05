package fetcher

import com.netaporter.uri.dsl._
import dispatch.{Http, url}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.{Elem, XML}

case class ISiteResponse(status: Int, contentType: String, fileId: String, xml: Elem)

class ISiteClient(conf: ISiteConfig, http: Http) {

  def get(contentId: String): Future[ISiteResponse] = {
    val uri = conf.baseUrl ? ("contentId" -> contentId) & ("api_key" -> conf.apiKey) & ("allowNonLive" -> true)
    val request = url(uri.toString()).GET

    http(request).map(response => {
      val xml = XML.loadString(response.getResponseBody)
      ISiteResponse(response.getStatusCode, xml \\ "metadata" \ "type" text, xml \\ "metadata" \ "fileId" text, xml)
    })
  }
}
