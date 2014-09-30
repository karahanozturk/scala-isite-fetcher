package fetcher

import com.redis.RedisClient

class DB(cache: RedisClient) {

  def updateSet(key: String, pids: Set[String]) = {
    cache.pipeline { p =>
      p.del(key)
      p.sadd(key, pids)
    }
  }

  def save(key: String, value: String) = cache.set(key, value)

  def remove(key: String) = cache.del(key)
}
