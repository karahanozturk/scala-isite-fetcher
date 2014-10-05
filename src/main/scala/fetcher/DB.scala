package fetcher

import com.redis.RedisClient

trait DB[A] {
  val cache: RedisClient
  def save(key: String, value: A)
  def remove(key: String) = cache.del(key)
}

case class SetDB(cache: RedisClient) extends DB[Set[String]] {
  def save(key: String, value: Set[String]) = cache.pipeline { pipeline =>
    pipeline.del(key)
    pipeline.sadd(key, value)
  }
}

case class StringDB(cache: RedisClient) extends DB[String] {
  def save(key: String, value: String) = cache.set(key, value)
}