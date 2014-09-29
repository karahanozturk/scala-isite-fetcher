package fetcher

import dispatch.{Http, url}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Response(status: Int, body: String)

class ISiteClient {
  def get(urlString: String): Future[Response] = {
    Http(url(urlString).GET).map(response => Response(response.getStatusCode, response.getResponseBody))
  }
}
