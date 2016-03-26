/*
 * Copyright 2014 – 2016 Paul Horn
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

import scalaz.@@

import transducers.{AsTarget, Arbitraries}

import org.specs2._
import org.specs2.mutable.Specification

import scala.math.{max, min}
import java.util.concurrent.atomic.AtomicInteger

object TransducersSpec extends Specification with ScalaCheck with Arbitraries with TransducerTests {

  "empty" should {
    val tx = transducers.empty[Int, Int]

    "produce nothing" in prop { (xs: List[Int]) ⇒
      run(xs, tx).length ==== 0
    }

    "consume nothing" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== 0
    }

    "show itself in toString" in {
      tx.toString ==== "(empty)"
    }

    "set the reduced state before reducing" in {
      val list = AsTarget[List]
      val reducer = list.reducer[Int]
      val reduced = new Reduced
      val empty = new EmptyReducer[Int, list.RB[Int]](reducer)
      val result = empty.prepare(list.empty[Int], reduced)

      list.finish(result) ==== List()
      reduced.? ==== true
    }

    "set the reduced state while reducing" in {
      val list = AsTarget[List]
      val reducer = list.reducer[Int]
      val reduced = new Reduced
      val empty = new EmptyReducer[Int, list.RB[Int]](reducer)
      val result = empty(list.empty[Int], 0, reduced)

      list.finish(result) ==== List()
      reduced.? ==== true
    }

    "fail when wrongly called in a reduce step" in {
      val list = AsTarget[List]
      val reducer = list.reducer[Int]
      val reduced = new Reduced
      val empty = new EmptyReducer[Int, list.RB[Int]](reducer)
      val result = empty.prepare(list.empty[Int], reduced)

      empty(result, 0, reduced) must throwA[IllegalStateException].like {
        case e ⇒ e.getMessage must startWith("ContractViolation")
      }
    }
  }

  "noop" should {
    val tx = transducers.noop[Int]

    "produce the input items" in prop { (xs: List[Int]) ⇒
      val result = run(xs, tx)
      result ==== xs
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in {
      tx.toString ==== "(noop)"
    }

    "remove itself under composition" in {
      val mapped = tx.map(_ + 1)
      mapped.toString ==== "(map)"
    }
  }

  "orElse" should {
    val tx = transducers.orElse(42)

    "produce at least one item" in prop { (xs: List[Int]) ⇒
      val result = run(xs, tx)
      result.length ==== max(xs.length, 1)
        result.last ==== xs.lastOption.getOrElse(42)
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in {
      tx.toString ==== "(orElse)"
    }
  }

  "foreach" should {
    val tx = transducers.foreach[Int](_ ⇒ ())

    "produce nothing" in prop { (xs: List[Int]) ⇒
      run(xs, tx).length ==== 0
    }

    "run a side-effect for each value" in prop { (xs: List[Int]) ⇒
      val sideEffects = new AtomicInteger
      val tx_! = transducers.foreach[Int](_ ⇒ sideEffects.incrementAndGet())
      run(xs, tx_!)
      sideEffects.get ==== xs.length
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in {
      tx.toString ==== "(foreach)"
    }
  }

  "map" should {
    "produce the input mapped over" in prop { (xs: List[Int], f: Int ⇒ Int) ⇒
      run(xs, transducers.map(f)) ==== xs.map(f)
    }

    "consume every item" in prop { (xs: List[Int], f: Int ⇒ Int) ⇒
      consume(xs, transducers.map(f)) ==== xs.length
    }

    "show itself in toString" in prop { (f: Int ⇒ Int) ⇒
      transducers.map(f).toString ==== "(map)"
    }
  }

  "flatMap" should {
    "produce the input flat-mapped over" in prop { (xs: List[Int], f: Int ⇒ List[Int]) ⇒
      run(xs, transducers.flatMap(f)) ==== xs.flatMap(f)
    }

    "consume every item" in prop { (xs: List[Int], f: Int ⇒ List[Int]) ⇒
      consume(xs, transducers.flatMap(f)) ==== xs.length
    }

    "show itself in toString" in prop { (f: Int ⇒ List[Int]) ⇒
      transducers.flatMap(f).toString ==== "(flatMap)"
    }
  }

  "filter" should {
    "produce the filtered input" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.filter(f)) ==== xs.filter(f)
    }

    "consume every item" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.filter(f)) ==== xs.length
    }

    "show itself in toString" in prop { (f: Int ⇒ Boolean) ⇒
      transducers.filter(f).toString ==== "(filter)"
    }
  }

  "filterNot" should {
    "produce the filtered input" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.filterNot(f)) ==== xs.filterNot(f)
    }

    "consume every item" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.filterNot(f)) ==== xs.length
    }

    "show itself in toString" in prop { (f: Int ⇒ Boolean) ⇒
      transducers.filterNot(f).toString ==== "(filter)"
    }
  }

  "collect" should {
    "produce the collected input" in prop { (xs: List[Int], f: Int =?> Int) ⇒
      run(xs, transducers.collect(f)) ==== xs.collect(f)
    }

    "consume every item" in prop { (xs: List[Int], f: Int =?> Int) ⇒
      consume(xs, transducers.collect(f)) ==== xs.length
    }

    "show itself in toString" in prop { (f: Int =?> Int) ⇒
      transducers.collect(f).toString ==== "(collect)"
    }
  }

  "collectFirst" should {
    "produce only the first value of the collected input" in prop { (xs: List[Int], f: Int =?> Int) ⇒
      run(xs, transducers.collectFirst(f)) ==== xs.collect(f).take(1)
    }

    "consume only the items until the first match" in prop { (xs: List[Int], f: Int =?> Int) ⇒
      consume(xs, transducers.collectFirst(f)) ==== itemsUntilFound(xs, f.isDefinedAt)
    }

    "show its composition in toString" in prop { (f: Int =?> Int) ⇒
      transducers.collectFirst(f).toString ==== "(collect).(take 1)"
    }
  }

  "find" should {
    "produce only the first value found" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.find(f)) ==== xs.find(f).toList
    }

    "consume only the items until the first match" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.find(f)) ==== itemsUntilFound(xs, f)
    }

    "show its composition in toString" in prop { (f: Int ⇒ Boolean) ⇒
      transducers.find(f).toString ==== "(filter).(take 1)"
    }
  }

  "forall" should {
    "produce only only one boolean value" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.forall(f)) ==== List(xs.forall(f))
    }

    "consume only the items until the first short-cut can be found" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.forall(f)) ==== itemsUntilFound(xs, !f(_))
    }

    "show its composition in toString" in prop { (f: Int ⇒ Boolean) ⇒
      transducers.forall(f).toString ==== "(collect).(take 1).(orElse)"
    }
  }

  "exists" should {
    "produce only only one boolean value" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      run(xs, transducers.exists(f)) ==== List(xs.exists(f))
    }

    "consume only the items until the first short-cut can be found" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      consume(xs, transducers.exists(f)) ==== itemsUntilFound(xs, f)
    }

    "show its composition in toString" in prop { (f: Int ⇒ Boolean) ⇒
      transducers.exists(f).toString ==== "(collect).(take 1).(orElse)"
    }
  }

  "fold" should {
    "produce only only one result value" in prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      run(xs, transducers.fold(z)(f)) ==== List(xs.foldLeft(z)(f))
    }

    "consume every item" in prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      consume(xs, transducers.fold(z)(f)) ==== xs.length
    }

    "show its composition in toString" in prop { (f: (Int, Int) ⇒ Int, z: Int) ⇒
      transducers.fold(z)(f).toString ==== "(scan).(last)"
    }
  }

  "scan" should {
    "produce all the result values" in prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      run(xs, transducers.scan(z)(f)) ==== xs.scanLeft(z)(f)
    }

    "consume every item" in prop { (xs: List[Int], f: (Int, Int) ⇒ Int, z: Int) ⇒
      consume(xs, transducers.fold(z)(f)) ==== xs.length
    }

    "show itself in toString" in prop { (f: (Int, Int) ⇒ Int, z: Int) ⇒
      transducers.scan(z)(f).toString ==== "(scan)"
    }
  }

  "head" should {
    val tx = transducers.head[Int]

    "produce the first value only" in prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.headOption.toList
    }

    "consume the first item only" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.take(1).length
    }

    "show its composition in toString" in {
      tx.toString ==== "(take 1)"
    }
  }

  "last" should {
    val tx = transducers.last[Int]

    "produce the last value only" in prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.lastOption.toList
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in {
      tx.toString ==== "(last)"
    }
  }

  "init" should {
    val tx = transducers.init[Int]

    "produce all but the last value" in prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== (if (xs.isEmpty) List() else xs.init)
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show its composition in toString" in {
      tx.toString ==== "(dropRight 1)"
    }
  }

  "tail" should {
    val tx = transducers.tail[Int]

    "produce all but the first value" in prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== (if (xs.isEmpty) List() else xs.tail)
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show its composition in toString" in {
      tx.toString ==== "(drop 1)"
    }
  }

  "take" should {
    "produce the first n items" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.take[Int](n.toLong)
      run(xs, tx) ==== xs.take(n)
    }

    "consume only the first n items" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.take[Int](n.toLong)
      consume(xs, tx) ==== min(n, xs.length)
    }

    "show itself in toString" in prop { (n: Int @@ NonZeroPositive) ⇒
      transducers.take(n.toLong).toString ==== s"(take $n)"
    }

    "show its composition in toString" in prop { (n: Int @@ Negative) ⇒
      transducers.take(n.toLong).toString ==== "(empty)"
    }
  }

  "takeWhile" should {
    "produce the first items until the predicate fails" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.takeWhile[Int](f)
      run(xs, tx) ==== xs.takeWhile(f)
    }

    "consume only the first items" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.takeWhile[Int](f)
      consume(xs, tx) ==== itemsUntilFound(xs, !f(_))
    }

    "show itself in toString" in prop { (f: Int ⇒ Boolean) ⇒
      transducers.takeWhile(f).toString ==== "(takeWhile)"
    }
  }

  "takeRight" should {
    "produce the last n items" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.takeRight[Int](n)
      run(xs, tx) ==== xs.takeRight(n)
    }

    "consume every item" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.takeRight[Int](n)
      consume(xs, tx) ==== (if ((n: Int) == 0) 0 else xs.length)
    }

    "show itself in toString" in prop { (n: Int @@ NonZeroPositive) ⇒
      transducers.takeRight(n).toString ==== s"(takeRight $n)"
    }

    "show its composition in toString" in prop { (n: Int @@ Negative) ⇒
      transducers.takeRight(n).toString ==== "(empty)"
    }
  }

  "takeNth" should {
    "produce every n-th item" in prop { (xs: List[Int], n: Int @@ NonZeroPositive) ⇒
      val tx = transducers.takeNth[Int](n.toLong)
      run(xs, tx) ==== xs.grouped(n).flatMap(_.take(1)).toList
    }

    "consume every item" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.takeNth[Int](n.toLong)
      consume(xs, tx) ==== (if ((n: Int) == 0) 0 else xs.length)
    }

    "show itself in toString" in prop { (n: Int @@ AtLeastTwo) ⇒
      transducers.takeNth(n.toLong).toString ==== (if ((n: Int) == 1) "(noop)" else s"(takeNth $n)")
    }

    "show its composition in toString" in prop { (n: Int @@ OneOrLess) ⇒
      transducers.takeNth(n.toLong).toString ==== (if ((n: Int) == 1) "(noop)" else "(empty)")
    }
  }

  "drop" should {
    "produce the all but the first n items" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.drop[Int](n.toLong)
      run(xs, tx) ==== xs.drop(n)
    }

    "consume every item" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.drop[Int](n.toLong)
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in prop { (n: Int @@ NonZeroPositive) ⇒
      transducers.drop(n.toLong).toString ==== s"(drop $n)"
    }

    "show its composition in toString" in prop { (n: Int @@ Negative) ⇒
      transducers.drop(n.toLong).toString ==== "(noop)"
    }
  }

  "dropWhile" should {
    "produce the all but the first items until the predicate fails" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.dropWhile[Int](f)
      run(xs, tx) ==== xs.dropWhile(f)
    }

    "consume every item" in prop { (xs: List[Int], f: Int ⇒ Boolean) ⇒
      val tx = transducers.dropWhile[Int](f)
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in prop { (f: Int ⇒ Boolean) ⇒
      transducers.dropWhile(f).toString ==== "(dropWhile)"
    }
  }

  "dropRight" should {
    "produce the all but the last n items" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.dropRight[Int](n)
      run(xs, tx) ==== xs.dropRight(n)
    }

    "consume every item" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.dropRight[Int](n)
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in prop { (n: Int @@ NonZeroPositive) ⇒
      transducers.dropRight(n).toString ==== s"(dropRight $n)"
    }

    "show its composition in toString" in prop { (n: Int @@ Negative) ⇒
      transducers.dropRight(n).toString ==== "(noop)"
    }
  }

  "dropNth" should {
    "produce all items but every n-th" in prop { (xs: List[Int], n: Int @@ NonZeroPositive) ⇒
      val tx = transducers.dropNth[Int](n.toLong)
      run(xs, tx) ==== xs.grouped(n).flatMap(_.drop(1)).toList
    }

    "consume every item" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.dropNth[Int](n.toLong)
      consume(xs, tx) ==== (if ((n: Int) == 0 || (n: Int) == 1) 0 else xs.length)
    }

    "show itself in toString" in prop { (n: Int @@ AtLeastTwo) ⇒
      transducers.dropNth(n.toLong).toString ==== s"(dropNth $n)"
    }

    "show its composition in toString" in prop { (n: Int @@ OneOrLess) ⇒
      transducers.dropNth(n.toLong).toString ==== "(empty)"
    }
  }

  "slice" should {
    "produce a slice of the items" in prop { (xs: List[Int], xy: (Int, Int)) ⇒
      val (n, m) = xy
      val tx = transducers.slice[Int](n.toLong, m.toLong)
      run(xs, tx) ==== xs.slice(n, m)
    }

    "consume only the items till the end of the slice" in prop { (xs: List[Int], xy: (Int, Int)) ⇒
      val (n, m) = xy
      val toConsume = if (n >= m) 0 else min(m, xs.length)
      val tx = transducers.slice[Int](n.toLong, m.toLong)
      consume(xs, tx) ==== toConsume
    }

    "show its composition in toString" in prop { (xy: (Int, Int)) ⇒
      val (n, m) = xy
      val expected =
        if (m <= n) "(empty)"
        else if (n == 0) s"(take $m)"
        else s"(drop $n).(take ${m - n})"
      transducers.slice(n.toLong, m.toLong).toString ==== expected
    }
  }

  "distinct" should {
    val tx = transducers.distinct[Int]

    "produce only the distinct set of items" in prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.foldLeft(Vector.empty[Int]) { (ys, x) ⇒
        if (ys.lastOption.contains(x)) ys else ys :+ x
      }.toList
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in {
      tx.toString ==== "(distinct)"
    }
  }

  "zipWithIndex" should {
    val tx = transducers.zipWithIndex[Int]

    "produce zipped pairs of items and their index" in prop { (xs: List[Int]) ⇒
      run(xs, tx) ==== xs.zipWithIndex
    }

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "show itself in toString" in {
      tx.toString ==== "(zipWithIndex)"
    }
  }

  "grouped" should {
    "produce groups of fixed size" in prop { (xs: List[Int], n: Int @@ NonZeroPositive) ⇒
      val tx = transducers.grouped[Int, List](n)
      run(xs, tx) ==== xs.grouped(n).toList
    }

    "consume every item" in prop { (xs: List[Int], n: Int @@ Positive) ⇒
      val tx = transducers.grouped[Int, List](n)
      consume(xs, tx) ==== (if ((n: Int) == 0) 0 else xs.length)
    }

    "show itself in toString" in prop { (n: Int @@ NonZeroPositive) ⇒
      transducers.grouped[Int, List](n).toString ==== s"(grouped $n)"
    }

    "show its composition in toString" in prop { (n: Int @@ Negative) ⇒
      transducers.grouped[Int, List](n).toString ==== s"(empty)"
    }
  }

  "groupBy" should {
    val f: Int ⇒ String = x ⇒ (x / 10).toString
    val tx = transducers.groupBy[Int, String, Vector](f)

    "produce groups of items by a key" in prop { (xs: List[Int]) ⇒
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

    "consume every item" in prop { (xs: List[Int]) ⇒
      consume(xs, tx) ==== xs.length
    }

    "stop buffering when downstream cancels the transducing" in {
      val tx2 = tx.take(1L)
      consume((1 to 20).toList, tx2) ==== 10
    }

    "show itself in toString" in {
      tx.toString ==== s"(groupBy)"
    }
  }
}
