package fetcher

case class QConfig(url: String,
                 waitTimeSeconds: Int,
                 maxNumberOfMessages: Int,
                 pollingDelay: Int)

case class PidListHandlerConfig(contentId: String, xmlContentType: String, cacheKey: String)
