package am.panov.cache

final case class CacheNode[K, V](
    key: K,
    value: V,
    var prev: Option[CacheNode[K, V]],
    var next: Option[CacheNode[K, V]]
)
