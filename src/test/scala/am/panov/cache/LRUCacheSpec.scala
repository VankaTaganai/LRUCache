package am.panov.cache

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class LRUCacheSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "LRUCache" - {
    "Creating with zero capacity will fail" in {
      cacheIO(0).assertThrows[IllegalArgumentException]
    }

    "Get will fail on empty cache" in {
      (for {
        cache <- cacheIO(5)
        res   <- cache.get(1)
      } yield res).assertThrows[IllegalArgumentException]
    }

    "Get will return \'ABC\' after adding that" in {
      (for {
        cache <- cacheIO(5)
        _     <- cache.put(1, "ABC")
        res   <- cache.get(1)
      } yield res).asserting(_ shouldBe "ABC")
    }

    "Get will replace least recently used value" in {
      (for {
        cache <- cacheIO(3)
        _     <- cache.put(1, "A")
        _     <- cache.put(2, "B")
        _     <- cache.put(3, "C")
        _     <- cache.put(4, "D")
        r2    <- cache.get(2)
        r3    <- cache.get(3)
        r4    <- cache.get(4)
      } yield (r2, r3, r4)).asserting(_ shouldBe ("B", "C", "D"))
    }

    "Get will change priority of element" in {
      (for {
        cache <- cacheIO(3)
        _     <- cache.put(1, "A")
        _     <- cache.put(2, "B")
        _     <- cache.put(3, "C")
        _     <- cache.get(1)
        _     <- cache.put(4, "D")
        r1    <- cache.get(1)
        r3    <- cache.get(3)
        r4    <- cache.get(4)
      } yield (r1, r3, r4)).asserting(_ shouldBe ("A", "C", "D"))
    }
  }

  def cacheIO(capacity: Int): IO[LRUCache[IO, Int, String]] =
    LRUCache.make[IO, Int, String](capacity)

}
