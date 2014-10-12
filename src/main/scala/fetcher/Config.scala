package fetcher

case class QueueConfig(url: String, waitTimeSeconds: Int, maxNumberOfMessages: Int, pollingDelay: Int)
case class MsgHandlerConfig(msgType: String, cacheKeyPrefix: String)
case class ISiteConfig(baseUrl: String, apiKey: String)