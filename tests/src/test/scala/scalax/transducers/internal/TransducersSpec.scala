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

package scalax
package transducers.internal

import transducers.{Transducer, Arbitraries}

import org.scalacheck.Gen.Choose
import org.scalacheck.{Arbitrary, Gen}
import org.specs2._
import org.specs2.mutable.Specification

import scala.math.{max, min}
import java.util.concurrent.atomic.AtomicInteger

final class TransducersSpec extends Specification with ScalaCheck with Arbitraries {
  import TransducersSpec._

  "empty" should {
    val tx = transducers.empty[Int]

    "produce nothing" ! prop { (xs: List[Int]) ⇒
      run(xs, tx).length ==== 0
    }

    "consume nothing" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== 0
    }
  }

  "orElse" should {
    val tx = transducers.orElse(42)

    "produce at least one item" ! prop { (xs: List[Int]) ⇒
      val result = run(xs, tx)
      result.length ==== max(xs.length, 1)
        result.last ==== xs.lastOption.getOrElse(42)
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }
  }

  "foreach" should {
    val tx = transducers.foreach[Int](_ ⇒ ())

    "produce nothing" ! prop { (xs: List[Int]) ⇒
      run(xs, tx).length ==== 0
    }

    "run a side-effect for each value" ! prop { (xs: List[Int]) ⇒
      val sideEffects = new AtomicInteger
      val tx_! = transducers.foreach[Int](_ ⇒ sideEffects.incrementAndGet())
      run(xs, tx_!)
      sideEffects.get ==== xs.length
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }
  }

  "map" should {
    "produce the input mapped over" ! prop { (xs: List[Int], f: Int ⇒ Int) ⇒
      run(xs, transducers.map(f)) ==== xs.map(f)
    }

    "consume every item" ! prop { (xs: List[Int], f: Int ⇒ Int) ⇒
      consume(xs, transducers.map(f)) ==== xs.length
    }
  }

  "flatMap" should {
    "produce the input flat-mapped over" ! prop { (xs: List[Int], f: Int ⇒ List[Int]) ⇒
      run(xs, transducers.flatMap(f)) ==== xs.flatMap(f)
    }

    "consume every item" ! prop { (xs: List[Int], f: Int ⇒ List[Int]) ⇒
      consume(xs, transducers.flatMap(f)) ==== xs.length
    }
  }

  "filter" should {
    "produce the filtered input" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.filter(f)) ==== xs.filter(f)
    }

    "consume every item" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.filter(f)) ==== xs.length
    }
  }

  "filterNot" should {
    "produce the filtered input" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.filterNot(f)) ==== xs.filterNot(f)
    }

    "consume every item" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.filterNot(f)) ==== xs.length
    }
  }

  "collect" should {
    "produce the collected input" ! prop { (xs: List[Int], f: Int =?> Int) ⇒
      run(xs, transducers.collect(f)) ==== xs.collect(f)
    }

    "consume every item" ! prop { (xs: List[Int], f: Int =?> Int) ⇒
      consume(xs, transducers.collect(f)) ==== xs.length
    }
  }

  "collectFirst" should {
    "produce only the first value of the collected input" ! prop { (xs: List[Int], f: Int =?> Int) ⇒
      run(xs, transducers.collectFirst(f)) ==== xs.collect(f).take(1)
    }

    "consume only the items until the first match" ! prop { (xs: List[Int], f: Int =?> Int) ⇒
      consume(xs, transducers.collectFirst(f)) ==== itemsUntilFound(xs, f.isDefinedAt)
    }
  }

  "find" should {
    "produce only the first value found" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.find(f)) ==== xs.find(f).toList
    }

    "consume only the items until the first match" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.find(f)) ==== itemsUntilFound(xs, f)
    }
  }

  "forall" should {
    "produce only only one boolean value" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.forall(f)) ==== List(xs.forall(f))
    }

    "consume only the items until the first short-cut can be found" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.forall(f)) ==== itemsUntilFound(xs, !f(_))
    }
  }

  "exists" should {
    "produce only only one boolean value" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.exists(f)) ==== List(xs.exists(f))
    }

    "consume only the items until the first short-cut can be found" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.exists(f)) ==== itemsUntilFound(xs, f)
    }
  }

  "fold" should {
    "produce only only one result value" ! prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      run(xs, transducers.fold(z)(f)) ==== List(xs.foldLeft(z)(f))
    }

    "consume every item" ! prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      consume(xs, transducers.fold(z)(f)) ==== xs.length
    }
  }

  "scan" should {
    "produce all the result values" ! prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      run(xs, transducers.scan(z)(f)) ==== xs.scanLeft(z)(f)
    }

    "consume every item" ! prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      consume(xs, transducers.fold(z)(f)) ==== xs.length
    }
  }

  "head" should {
    val tx = transducers.head[Int]

    "produce the first value only" ! prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.headOption.toList
    }

    "consume the first item only" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.take(1).length
    }
  }

  "last" should {
    val tx = transducers.last[Int]

    "produce the last value only" ! prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.lastOption.toList
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }
  }

  "init" should {
    val tx = transducers.init[Int]

    "produce all but the last value" ! prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== (if (xs.isEmpty) List() else xs.init)
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }
  }

  "tail" should {
    val tx = transducers.tail[Int]

    "produce all but the first value" ! prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== (if (xs.isEmpty) List() else xs.tail)
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }
  }

  "take" should {
    "produce the first n items" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.take[Int](n.toLong)
      run(xs, tx) ==== xs.take(n)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)

    "consume only the first n items" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.take[Int](n.toLong)
      consume(xs, tx) ==== min(n, xs.length)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)
  }

  "takeWhile" should {
    "produce the first items until the predicate fails" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.takeWhile[Int](f)
      run(xs, tx) ==== xs.takeWhile(f)
    }

    "consume only the first items" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.takeWhile[Int](f)
      consume(xs, tx) ==== itemsUntilFound(xs, !f(_))
    }
  }

  "takeRight" should {
    "produce the last n items" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.takeRight[Int](n)
      run(xs, tx) ==== xs.takeRight(n)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)

    "consume every item" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.takeRight[Int](n)
      consume(xs, tx) ==== (if (n == 0) 0 else xs.length)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)
  }

  "takeNth" should {
    "produce every n-th item" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.takeNth[Int](n.toLong)
      run(xs, tx) ==== xs.grouped(n).flatMap(_.take(1)).toList
    }(implicitly, implicitly, implicitly, posNonZeroNum, implicitly, implicitly)

    "consume every item" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.takeNth[Int](n.toLong)
      consume(xs, tx) ==== (if (n == 0) 0 else xs.length)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)
  }

  "drop" should {
    "produce the all but the first n items" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.drop[Int](n.toLong)
      run(xs, tx) ==== xs.drop(n)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)

    "consume every item" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.drop[Int](n.toLong)
      consume(xs, tx) ==== xs.length
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)
  }

  "dropWhile" should {
    "produce the all but the first items until the predicate fails" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.dropWhile[Int](f)
      run(xs, tx) ==== xs.dropWhile(f)
    }

    "consume every item" ! prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.dropWhile[Int](f)
      consume(xs, tx) ==== xs.length
    }
  }

  "dropRight" should {
    "produce the all but the last n items" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.dropRight[Int](n)
      run(xs, tx) ==== xs.dropRight(n)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)

    "consume every item" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.dropRight[Int](n)
      consume(xs, tx) ==== xs.length
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)
  }

  "dropNth" should {
    "produce all items but every n-th" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.dropNth[Int](n.toLong)
      run(xs, tx) ==== xs.grouped(n).flatMap(_.drop(1)).toList
    }(implicitly, implicitly, implicitly, posNonZeroNum, implicitly, implicitly)

    "consume every item" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.dropNth[Int](n.toLong)
      consume(xs, tx) ==== (if (n == 0 || n == 1) 0 else xs.length)
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)
  }

  "slice" should {
    "produce a slice of the items" ! prop { (xs: List[Int], x: Int, y: Int) ⇒
      val (n, m) = if (x > y) (y, x) else (x, y)
      val tx = transducers.slice[Int](n.toLong, m.toLong)
      run(xs, tx) ==== xs.slice(n, m)
    }(implicitly, implicitly, implicitly, posNum, implicitly, posNum, implicitly, implicitly)

    "consume only the items till the end of the slice" ! prop { (xs: List[Int], x: Int, y: Int) ⇒
      val (n, m) = if (x > y) (y, x) else (x, y)
      val toConsume = if (n >= m) 0 else min(m, xs.length)
      val tx = transducers.slice[Int](n.toLong, m.toLong)
      consume(xs, tx) ==== toConsume
    }(implicitly, implicitly, implicitly, posNum, implicitly, posNum, implicitly, implicitly)
  }

  "distinct" should {
    val tx = transducers.distinct[Int]

    "produce only the distinct set of items" ! prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.foldLeft(Vector.empty[Int]) { (ys, x) ⇒
        if (ys.lastOption.contains(x)) ys else ys :+ x
      }.toList
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }
  }

  "zipWithIndex" should {
    val tx = transducers.zipWithIndex[Int]

    "produce zipped pairs of items and their index" ! prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.zipWithIndex
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }
  }

  "grouped" should {
    "produce groups of fixed size" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.grouped[Int, List](n)
      run(xs, tx) ==== xs.grouped(n).toList
    }(implicitly, implicitly, implicitly, posNonZeroNum, implicitly, implicitly)

    "consume every item" ! prop { (xs: List[Int], n: Int) ⇒
      val tx = transducers.grouped[Int, List](n)
      consume(xs, tx) ==== xs.length
    }(implicitly, implicitly, implicitly, posNum, implicitly, implicitly)
  }

  "groupBy" should {
    "produce groups of items by a key" ! prop { (xs: List[Int]) ⇒
      val f: Int ⇒ String = x ⇒ (x / 10).toString
      val tx = transducers.groupBy[Int, String, Vector](f)
      run(xs, tx) ==== xs.foldLeft(Vector.empty[(String, Vector[Int])]) { (ys, x) ⇒
        val key = f(x)
        if (ys.isEmpty)
          Vector((key, Vector(x)))
        else if (ys.last._1 == key)
          ys.init :+ (key → (ys.last._2 :+ x))
        else
          ys :+ (key → Vector(x))
      }.map(_._2).toList
    }

    "consume every item" ! prop { (xs: List[Int]) ⇒
      val f: Int ⇒ String = x ⇒ (x / 10).toString
      val tx = transducers.groupBy[Int, String, Vector](f)
      consume(xs, tx) ==== xs.length
    }
  }

  private val posNum =
    Arbitrary(Gen.sized(max ⇒ Choose.chooseInt.choose(0, max * 2)))

  private val posNonZeroNum =
    Arbitrary(Gen.sized(max ⇒ Choose.chooseInt.choose(1, max * 2)))

  private def run[A](xs: List[Int], tx: Transducer[Int, A]) =
    transducers.run(tx)(xs)

  private def consume(xs: List[Int], tx: Transducer[Int, _]) = {
    val it = new CountingIterator(xs)
    transducers.run(tx)(it)
    it.consumed
  }

  private def itemsUntilFound(xs: List[Int], f: Int ⇒ Boolean): Int = {
    val (dropped, rest) = xs.span(!f(_))
    dropped.length + rest.take(1).length
  }
}
object TransducersSpec {
  class CountingIterator(xs: List[Int]) extends Iterator[Int] {
    private[this] final val iter = xs.iterator
    private[this] final var _consumed = 0
    def hasNext: Boolean = iter.hasNext
    def next(): Int = {
      _consumed += 1
      iter.next()
    }
    def consumed: Int = _consumed
  }
}
