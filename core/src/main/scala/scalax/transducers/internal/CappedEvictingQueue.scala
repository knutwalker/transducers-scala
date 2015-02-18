/*
 * Copyright 2014 Paul Horn
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

final class CappedEvictingQueue[A](private val capacity: Int) {

  import scalax.transducers.internal.CappedEvictingQueue.elementsIterator

  private val backing = new Array[Any](capacity)
  private val max = capacity.toLong
  private var cursor = 0L

  def add(elem: A): Option[A] =
    if (overCapacity)
      addEvictingly(elem)
    else
      addNormally(elem)

  private def overCapacity: Boolean =
    cursor >= capacity

  private def addEvictingly(elem: A): Option[A] = {
    val head = (cursor % max).toInt
    val oldest = backing(head)
    backing(head) = elem
    cursor += 1L
    Some(oldest).asInstanceOf[Option[A]]
  }

  private def addNormally(elem: A): Option[A] = {
    backing(cursor.toInt) = elem
    cursor += 1L
    None
  }

  def elements: Iterator[A] =
    if (overCapacity)
      elementsIterator(this, (cursor % max).toInt, capacity)
    else
      elementsIterator(this, 0, cursor.toInt)

  override def toString = {
    val head = (cursor % max).toInt
    val current = backing(head)
    backing.map(String.valueOf).updated(head, s"($current)").mkString("[", ", ", "]")
  }
}

private object CappedEvictingQueue {

  def elementsIterator[A](q: CappedEvictingQueue[A], fromIndex: Int, nrElements: Int): Iterator[A] =
    new QueueElementsIterator[A](fromIndex, nrElements, q.capacity, q.backing)

  class QueueElementsIterator[A](fromIndex: Int, nrElements: Int, capacity: Int, backing: Array[Any]) extends Iterator[A] {
    private var drained = 0
    private var current = fromIndex

    def hasNext = drained < nrElements

    def next() = {
      val index = current % capacity
      val elem = backing(index)
      current += 1
      drained += 1
      elem.asInstanceOf[A]
    }

    override def hasDefiniteSize = true

    override def size = nrElements - drained
  }

}
