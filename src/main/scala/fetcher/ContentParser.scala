package fetcher

import com.amazonaws.services.sqs.model.Message

class ContentParser {
  def parsePids(xmlBody: String): List[String] = ???
  def extractNotification(message: Message): Notification = ???
}