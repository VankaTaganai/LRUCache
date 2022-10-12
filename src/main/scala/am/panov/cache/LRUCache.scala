package am.panov.cache

import cats.effect.Concurrent
import cats.effect.Sync
import cats.syntax.all._
import scala.collection.mutable.{Map => MMap}

final class LRUCache[F[_]: Sync, K, V] private (
    capacity: Int,
    cache: MMap[K, CacheNode[K, V]],
    private var leastUsed: Option[CacheNode[K, V]],
    private var mostUsed: Option[CacheNode[K, V]]
) {

  def get(key: K): F[V] =
    for {
      valueO <- Sync[F].delay(cache.get(key))
      value <- valueO.fold(
        Sync[F].raiseError[CacheNode[K, V]](new IllegalArgumentException("Value with this key doesn't exist"))
      )(a => Sync[F].pure(a))
      _ <- remove(value)
      _ <- add(value)
    } yield value.value

  def put(key: K, value: V): F[Unit] =
    for {
      _ <- cache.get(key).fold(Sync[F].unit)(remove)
      newNode = CacheNode(key, value, None, None)
      _ <- add(newNode)
      _ <- Sync[F].delay(assert(cache.contains(key)))
    } yield ()

  private def remove(node: CacheNode[K, V]): F[Unit] =
    Sync[F].delay {
      assert(cache.contains(node.key))

      cache.remove(node.key)

      val prev = node.prev
      val next = node.next

      prev.foreach(p => p.next = next)
      next.foreach(n => n.prev = prev)

      if (prev.isEmpty) leastUsed = next
      if (next.isEmpty) mostUsed = prev

      assert(!cache.contains(node.key))
    }

  private def add(node: CacheNode[K, V]): F[Unit] =
    for {
      _ <- Sync[F].delay(assert(!cache.contains(node.key)))
      _ <- if (cache.size == capacity) remove(leastUsed.get) else Sync[F].unit
      _ <- Sync[F].delay {
        cache.put(node.key, node)
        node.prev = mostUsed
        node.next = None
        mostUsed.foreach(m => m.next = Some(node))
        mostUsed = Some(node)
        if (cache.size == 1) leastUsed = mostUsed
      }
      _ <- Sync[F].delay(assert(cache.contains(node.key)))
    } yield ()

}

object LRUCache {
  def make[F[_]: Sync: Concurrent, K, V](capacity: Int): F[LRUCache[F, K, V]] =
    if (capacity <= 0)
      Sync[F].raiseError(new IllegalArgumentException("Capacity should be greater then 0"))
    else {
      val cache     = MMap.empty[K, CacheNode[K, V]]
      val firstNode = Option.empty[CacheNode[K, V]]
      val lastNode  = Option.empty[CacheNode[K, V]]

      Sync[F].delay(new LRUCache[F, K, V](capacity, cache, firstNode, lastNode))
    }

}
