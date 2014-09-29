package fetcher

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.{Message, ReceiveMessageRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class Queue(sqs: AmazonSQSClient, qConf: QConfig) {

  def pollMessage() = Future {
    val request = new ReceiveMessageRequest(qConf.url)
    request.setWaitTimeSeconds(qConf.waitTimeSeconds)
    request.setMaxNumberOfMessages(qConf.maxNumberOfMessages)
    sqs.receiveMessage(request).getMessages.get(0)
  }

  def deleteMessage(msg: Message) = Future {
    sqs.deleteMessage("queueUrl", msg.getReceiptHandle)
  }
}
