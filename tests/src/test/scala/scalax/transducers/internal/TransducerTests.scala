/*
 * Copyright 2014 – 2018 Paul Horn
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

import collection.GenIterableLike
import scalax.transducers
import scalax.transducers.Transducer

trait TransducerTests {

  protected def run[A](xs: List[Int], tx: Transducer[Int, A]) =
    transducers.run(tx)(xs)

  protected def consume(xs: List[Int], tx: Transducer[Int, _]) = {
    val it = CountingIterator(xs)
    transducers.run(tx)(it.it)
    it.consumed
  }

  protected def itemsUntilFound(xs: List[Int], f: Int ⇒ Boolean): Int = {
    val (dropped, rest) = xs.span(!f(_))
    dropped.length + rest.take(1).length
  }

  class CountingIterator[A](xs: GenIterableLike[A, _]) extends Iterator[A] {
    private[this] final val iter = xs.iterator
    private[this] final var _consumed = 0
    def hasNext: Boolean = iter.hasNext
    def next(): A = {
      _consumed += 1
      iter.next()
    }
    def consumed: Int = _consumed

    def it: Iterator[A] = this
  }

  object CountingIterator {
    def apply[A](xs: GenIterableLike[A, _]): CountingIterator[A] =
      new CountingIterator[A](xs)
  }
}
object TransducerTests extends TransducerTests
