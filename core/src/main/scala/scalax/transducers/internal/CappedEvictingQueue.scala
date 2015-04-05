/*
 * Copyright 2014 – 2015 Paul Horn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scalax.transducers.internal

final class CappedEvictingQueue[A](private[this] val capacity: Int) {
  require(capacity > 0, "capacity must be at least 1")

  private[this] val backing = new Array[Any](capacity)
  private[this] val max = capacity.toLong
  private[this] var cursor = 0L

  def add(elem: A): Option[A] =
    if (overCapacity)
      addEvictingly(elem)
    else
      addNormally(elem)

  private[this] def overCapacity: Boolean =
    cursor >= capacity

  private[this] def addEvictingly(elem: A): Option[A] = {
    val head = (cursor % max).toInt
    val oldest = backing(head)
    backing(head) = elem
    cursor += 1L
    Some(oldest).asInstanceOf[Option[A]]
  }

  private[this] def addNormally(elem: A): Option[A] = {
    backing(cursor.toInt) = elem
    cursor += 1L
    None
  }

  def size: Int =
    scala.math.min(cursor, max).toInt

  def isEmpty: Boolean =
    cursor == 0L

  def nonEmpty: Boolean =
    cursor > 0L

  def elements: Iterator[A] =
    if (overCapacity)
      new CappedEvictingQueue.QueueElementsIterator[A]((cursor % max).toInt, capacity, capacity, backing)
    else
      new CappedEvictingQueue.QueueElementsIterator[A](0, cursor.toInt, capacity, backing)

  override def toString: String = {
    val head = (cursor % max).toInt
    val current = backing(head)
    backing.map(String.valueOf).updated(head, s"($current)").mkString("[", ", ", "]")
  }
}

private[this] object CappedEvictingQueue {

  class QueueElementsIterator[A](fromIndex: Int, nrElements: Int, capacity: Int, backing: Array[Any]) extends Iterator[A] {
    private[this] var drained = 0
    private[this] var current = fromIndex

    def hasNext: Boolean = drained < nrElements

    def next(): A = {
      val index = current % capacity
      val elem = backing(index)
      current += 1
      drained += 1
      elem.asInstanceOf[A]
    }

    override def hasDefiniteSize: Boolean = true

    override def size: Int = nrElements - drained
  }

}
