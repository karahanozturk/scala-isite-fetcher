package fetcher

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Message(receiptHandle: String, notification: Notification)

class Queue(sqs: AmazonSQSClient, qConf: QConfig) {

  implicit val formats = org.json4s.DefaultFormats
  def extractString(json: JValue, query: String) = (json \\ query).extract[String]

  def extractNotification(sqsBody: String) = {
    val snsJson = parse(sqsBody)
    val snsMsgBody = parse(extractString(snsJson, "Message"))
    Notification(
      extractString(snsMsgBody, "type"),
      extractString(snsMsgBody, "projectId"),
      extractString(snsMsgBody, "contentId"))
  }

  def pollMessage() = Future {
    val request = new ReceiveMessageRequest(qConf.url).withWaitTimeSeconds(qConf.waitTimeSeconds).withMaxNumberOfMessages(qConf.maxNumberOfMessages)
    val sqsMsg = sqs.receiveMessage(request).getMessages.get(0)
    Message(sqsMsg.getReceiptHandle, extractNotification(sqsMsg.getBody))
  }

  def deleteMessage(msg: Message) = Future {
    sqs.deleteMessage(qConf.url, msg.receiptHandle)
  }
}
