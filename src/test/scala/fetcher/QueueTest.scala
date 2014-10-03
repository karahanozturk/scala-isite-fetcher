package fetcher

import java.util

import com.amazonaws.services.sqs.model.{ReceiveMessageRequest, ReceiveMessageResult}
import com.amazonaws.services.sqs.{AmazonSQSClient, model}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.io.Source

class QueueTest extends Specification with Mockito {

  val sqs = mock[AmazonSQSClient]
  val qConf = QConfig("url", 1, 1, 1)
  val queue = new Queue(sqs, qConf)

  val sqsMsg = Source.fromURL(getClass.getResource("/snsNotification")).mkString
  val request = new ReceiveMessageRequest(qConf.url).withWaitTimeSeconds(qConf.waitTimeSeconds).withMaxNumberOfMessages(qConf.maxNumberOfMessages)
  val sqsMessages = new util.ArrayList[Message]
  val message = new model.Message().withReceiptHandle("receiptHandle").withBody(sqsMsg)
  val result = new ReceiveMessageResult().withMessages(message)
  sqs.receiveMessage(any[ReceiveMessageRequest]) returns result

  "Queue" should {
    "extract notification from SQS message" in {
      val msg = Await.result(queue.pollMessage(), scala.concurrent.duration.DurationInt(1000).millis)
      msg.receiptHandle must equalTo("receiptHandle")
      msg.notification must equalTo(Notification("publish", "iplayer", "e56fce55-86cc-4f71-aa67-3157ddb70286"))
    }
  }

}
