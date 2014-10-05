package fetcher.handler

import fetcher.{MsgHandlerConfig, SetDB}

case class BlacklistHandler(db: SetDB, conf: MsgHandlerConfig)
  extends MessageHandler[Set[String]]
  with Publish[Set[String]]
  with SetContentParser {}

