package fetcher

import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.{AmazonSQSClient, model}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class Message(receiptHandle: String, notification: Notification)

class Queue(sqs: AmazonSQSClient, qConf: QConfig) {

  def extractNotification(body: String): Notification = ???

  def pollMessage() = Future {
    val request = new ReceiveMessageRequest(qConf.url).withWaitTimeSeconds(qConf.waitTimeSeconds).withMaxNumberOfMessages(qConf.maxNumberOfMessages)
    val sqsMsg: model.Message = sqs.receiveMessage(request).getMessages.get(0)
    Message(sqsMsg.getReceiptHandle, extractNotification(sqsMsg.getBody))
  }

  def deleteMessage(msg: Message) = Future {
    sqs.deleteMessage(qConf.url, msg.receiptHandle)
  }
}
