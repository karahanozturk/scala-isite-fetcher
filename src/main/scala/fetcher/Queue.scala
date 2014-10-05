package fetcher

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Message(projectId: String, publishType: String, contentId: String, receiptHandle: String)

class Queue(sqs: AmazonSQSClient, qConf: QueueConfig) {
  implicit val formats = org.json4s.DefaultFormats
  def extractString(json: JValue, query: String) = (json \\ query).extract[String]

  def pollMessage() = Future {
    val request = new ReceiveMessageRequest(qConf.url)
      .withWaitTimeSeconds(qConf.waitTimeSeconds)
      .withMaxNumberOfMessages(qConf.maxNumberOfMessages)
    val sqsMsg = sqs.receiveMessage(request).getMessages.get(0)
    val snsJson = parse(sqsMsg.getBody)
    val snsMsgBody = parse(extractString(snsJson, "Message"))

    Message(
      extractString(snsMsgBody, "projectId"),
      extractString(snsMsgBody, "type"),
      extractString(snsMsgBody, "contentId"),
      sqsMsg.getReceiptHandle)
  }

  def deleteMessage(msg: Message) = Future {
    sqs.deleteMessage(qConf.url, msg.receiptHandle)
  }
}
