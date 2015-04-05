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

package scalax.transducers
package internal

import scala.language.higherKinds

private[internal] abstract class Delegate[A, R](rf: Reducer[_, R]) extends Reducer[A, R] {
  final def apply(r: R): R = rf(r)
  def prepare(r: R, s: Reduced): R = rf.prepare(r, s)
}

private[internal] abstract class Buffer[A, R, F[_]](rf: Reducer[F[A], R])(implicit F: AsTarget[F]) extends Reducer[A, R] {
  private[this] var buffer = F.empty[A]

  final def apply(r: R): R =
    rf(if (F.nonEmpty(buffer)) rf(r, F.finish(buffer), new Reduced) else r)

  protected final def append(a: A): Unit =
    buffer = F.append(buffer, a)

  protected final def size: Int =
    F.size(buffer)

  protected final def flush(r: R, s: Reduced): R = {
    val ret = rf(r, F.finish(buffer), s)
    buffer = F.empty[A]
    ret
  }
}



private[internal] final class EmptyReducer[A, R](rf: Reducer[_, R]) extends Delegate[A, R](rf) {
  override def prepare(r: R, s: Reduced): R =
    s(r)

  def apply(r: R, a: A, s: Reduced): R =
    s(r)
}

private[internal] final class NoOpReducer[A, R](rf: Reducer[A, R]) extends Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced): R =
    rf(r, a, s)
}

private[internal] final class OrElseReducer[A, R](rf: Reducer[A, R], cont: ⇒ A) extends Reducer[A, R] {
  private[this] var hasValue = false

  def prepare(r: R, s: Reduced): R =
    rf.prepare(r, s)

  def apply(r: R, a: A, s: Reduced): R = {
    hasValue = true
    rf(r, a, s)
  }

  def apply(r: R): R = {
    val ret = if (!hasValue) rf(r, cont, new Reduced) else r
    rf(ret)
  }
}

private[internal] final class ForeachReducer[A, R](rf: Reducer[Unit, R], f: A ⇒ Unit) extends Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced): R = {
    f(a)
    r
  }
}

private[internal] final class MapReducer[B, A, R](rf: Reducer[B, R], f: A ⇒ B) extends Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced): R =
    rf(r, f(a), s)
}

private[internal] final class FlatMapReducer[A, B, R, F[_]: AsSource](rf: Reducer[B, R], f: A ⇒ F[B]) extends Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced): R =
    Reducing.reduceStep(rf, r, f(a), s)
}

private[internal] final class FilterReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced): R =
    if (f(a)) rf(r, a, s) else r
}

private[internal] final class CollectReducer[A, B, R](rf: Reducer[B, R], pf: PartialFunction[A, B]) extends Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced): R =
    if (pf.isDefinedAt(a)) rf(r, pf(a), s) else r
}

private[internal] final class ScanReducer[A, B, R](rf: Reducer[B, R], z: B, f: (B, A) ⇒ B) extends Delegate[A, R](rf) {
  private[this] var result = z

  override def prepare(r: R, s: Reduced): R =
    rf(r, result, s)

  def apply(r: R, a: A, s: Reduced): R = {
    result = f(result, a)
    rf(r, result, s)
  }
}

private[internal] final class TakeReducer[A, R](rf: Reducer[A, R], n: Long) extends Delegate[A, R](rf) {
  private[this] var taken = 1L

  def apply(r: R, a: A, s: Reduced): R =
    if (taken < n) {
      taken += 1
      rf(r, a, s)
    }
    else {
      s(rf(r, a, s))
    }
}

private[internal] final class TakeWhileReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Delegate[A, R](rf) {

  def apply(r: R, a: A, s: Reduced): R =
    if (f(a)) rf(r, a, s) else s(r)
}

private[internal] final class TakeNthReducer[A, R](rf: Reducer[A, R], n: Long) extends Delegate[A, R](rf) {
  private[this] var nth = 0L

  def apply(r: R, a: A, s: Reduced): R = {
    val res = if (nth % n == 0) rf(r, a, s) else r
    nth += 1
    res
  }
}

private[internal] final class TakeRightReducer[A, R](rf: Reducer[A, R], n: Int) extends Reducer[A, R] {
  private[this] val queue = new CappedEvictingQueue[A](n)

  def prepare(r: R, s: Reduced): R = rf.prepare(r, s)

  def apply(r: R, a: A, s: Reduced): R = {
    queue.add(a)
    r
  }

  def apply(r: R): R =
    Reducing.reduce(r, queue.elements)(rf)
}

private[internal] final class DropReducer[A, R](rf: Reducer[A, R], n: Long) extends Delegate[A, R](rf) {
  private[this] var dropped = 0L

  def apply(r: R, a: A, s: Reduced): R = {
    if (dropped < n) {
      dropped += 1
      r
    }
    else rf(r, a, s)
  }
}

private[internal] final class DropWhileReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Delegate[A, R](rf) {
  private[this] var drop = true

  def apply(r: R, a: A, s: Reduced): R =
    if (drop && f(a)) r
    else {
      drop = false
      rf(r, a, s)
    }
}

private[internal] final class DropNthReducer[A, R](rf: Reducer[A, R], n: Long) extends Delegate[A, R](rf) {
  private[this] var nth = 0L

  def apply(r: R, a: A, s: Reduced): R = {
    val res = if (nth % n == 0) r else rf(r, a, s)
    nth += 1
    res
  }
}

private[internal] final class DropRightReducer[A, R](rf: Reducer[A, R], n: Int) extends Delegate[A, R](rf) {
  private[this] val queue = new CappedEvictingQueue[A](n)

  def apply(r: R, a: A, s: Reduced): R =
    queue.add(a).fold(r)(rf(r, _, s))
}

private[internal] final class DistinctReducer[A, R](rf: Reducer[A, R]) extends Delegate[A, R](rf) {
  private[this] var previous: A = _

  def apply(r: R, a: A, s: Reduced): R =
    if (a != previous) {
      previous = a
      rf(r, a, s)
    }
    else r
}

private[internal] final class ZipWithIndexReducer[A, R](rf: Reducer[(A, Int), R]) extends Delegate[A, R](rf) {
  private[this] val ids = Iterator.from(0)

  def apply(r: R, a: A, s: Reduced): R =
    rf(r, (a, ids.next()), s)
}

private[internal] final class GroupedReducer[A, R, F[_]: AsTarget](rf: Reducer[F[A], R], n: Int) extends Buffer[A, R, F](rf) {

  def prepare(r: R, s: Reduced): R = rf.prepare(r, s)

  def apply(r: R, a: A, s: Reduced): R = {
    append(a)
    if (size == n) flush(r, s) else r
  }
}

private[internal] final class GroupByReducer[A, B <: AnyRef, R, F[_]: AsTarget](rf: Reducer[F[A], R], f: A ⇒ B) extends Buffer[A, R, F](rf) {
  private[this] val mark = new AnyRef
  private[this] var previous = mark

  def prepare(r: R, s: Reduced): R = rf.prepare(r, s)

  def apply(r: R, a: A, s: Reduced): R = {
    val key = f(a)
    val shouldAppend = previous == key || (previous eq mark)
    previous = key
    if (!shouldAppend) {
      val result = flush(r, s)
      if (!s.?) {
        append(a)
      }
      result
    } else {
      append(a)
      r
    }
  }
}
