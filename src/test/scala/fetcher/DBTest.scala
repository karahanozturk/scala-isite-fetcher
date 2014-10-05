package fetcher

import com.redis.RedisClient
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class DBTest extends Specification with Mockito {

  "SetDB" should {
    val key = "key"
    val value = Set("pid1")
    val cache = mock[RedisClient]
    val setDB = SetDB(cache)

    "replace the set when saved" in{
      setDB.save("key", value)

      there was one(cache).pipeline { p =>
        p.del(key)
        p.sadd(key, value)
      }
    }

    "delete the set when remove called" in{
      setDB.remove("key")
      there was one(cache).del(key)
    }
  }

  "StringDB" should {
    val key = "key"
    val value = "value"
    val cache = mock[RedisClient]
    val stringDB = StringDB(cache)

    "sets the string value by the given key when saved" in{
      stringDB.save("key", value)
      there was one(cache).set(key, value)
    }

    "delete the string value when remove called" in{
      stringDB.remove("key")
      there was one(cache).del(key)
    }
  }
}
