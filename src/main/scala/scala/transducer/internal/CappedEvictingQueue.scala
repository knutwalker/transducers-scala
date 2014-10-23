package scala.transducer.internal

import scala.reflect.ClassTag


final class CappedEvictingQueue[A: ClassTag](capacity: Int) {
  private val backing = new Array[A](capacity)
  private var cursor = 0L
  private val max = capacity.toLong

  def add(elem: A): Option[A] =
    if (cursor >= capacity)
      addEvictingly(elem)
    else
      addNormally(elem)

  override def toString = {
    val head = (cursor % max).toInt
    val current = backing(head)
    backing.map(String.valueOf).updated(head, s"($current)").mkString("[", ", ", "]")
  }

  private def addEvictingly(elem: A): Option[A] = {
    val head = (cursor % max).toInt
    val oldest = backing(head)
    backing(head) = elem
    cursor += 1L
    Some(oldest)
  }

  private def addNormally(elem: A): Option[A] = {
    backing(cursor.toInt) = elem
    cursor += 1L
    None
  }
}
