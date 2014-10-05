package fetcher

case class QueueConfig(url: String, waitTimeSeconds: Int, maxNumberOfMessages: Int, pollingDelay: Int)
case class MsgHandlerConfig(msgType: String, xmlContentType: String, cacheKey: String)
case class ISiteConfig(baseUrl: String, apiKey: String)