package fetcher.handler

import fetcher.{MsgHandlerConfig, ISiteContent}

trait DBIdGenerator {
  def id(content: ISiteContent, conf: MsgHandlerConfig): String
}

trait GenerateIdForAllContents extends DBIdGenerator {
  override def id(content: ISiteContent, conf: MsgHandlerConfig) = conf.cacheKey
}

trait GenerateIdForEachContent extends DBIdGenerator {
  override def id(content: ISiteContent, conf: MsgHandlerConfig) = conf.cacheKey + content.fileId
}
