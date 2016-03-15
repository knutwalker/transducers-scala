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

package scalax.transducers
package internal

import scala.language.higherKinds

private[transducers] final class CombinedTransducer[A, B, C](left: Transducer[A, B], right: Transducer[B, C]) extends Transducer[A, C] {
  def apply[R](rf: Reducer[C, R]): Reducer[A, R] =
    left(right(rf))

  override def toString: String = s"$left.$right"
}

private[transducers] final class EmptyTransducer[A, B] extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]): Reducer[A, R] =
    new EmptyReducer[A, R](rf)

  override def toString: String = "(empty)"
}

private[transducers] final class NoOpTransducer[A] extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new NoOpReducer[A, R](rf)

  override def toString: String = "(noop)"
}


private[transducers] final class OrElseTransducer[A](cont: ⇒ A) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new OrElseReducer[A, R](rf, cont)

  override def toString: String = "(orElse)"
}

private[transducers] final class ForeachTransducer[A](f: A ⇒ Unit) extends Transducer[A, Unit] {
  def apply[R](rf: Reducer[Unit, R]): Reducer[A, R] =
    new ForeachReducer[A, R](rf, f)

  override def toString: String = "(foreach)"
}

private[transducers] final class MapTransducer[A, B](f: A ⇒ B) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]): Reducer[A, R] =
    new MapReducer[B, A, R](rf, f)

  override def toString: String = "(map)"
}

private[transducers] final class FlatMapTransducer[A, B, F[_]: AsSource](f: A ⇒ F[B]) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]): Reducer[A, R] =
    new FlatMapReducer[A, B, R, F](rf, f)

  override def toString: String = "(flatMap)"
}

private[transducers] final class FilterTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new FilterReducer[A, R](rf, f)

  override def toString: String = "(filter)"
}

private[transducers] final class CollectTransducer[A, B](pf: PartialFunction[A, B]) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]): Reducer[A, R] =
    new CollectReducer[A, B, R](rf, pf)

  override def toString: String = "(collect)"
}

private[transducers] final class ScanTransducer[A, B](z: B, f: (B, A) ⇒ B) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]): Reducer[A, R] =
    new ScanReducer[A, B, R](rf, z, f)

  override def toString: String = "(scan)"
}

private[transducers] final class TakeTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new TakeReducer[A, R](rf, n)

  override def toString: String = s"(take $n)"
}

private[transducers] final class TakeWhileTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new TakeWhileReducer[A, R](rf, f)

  override def toString: String = "(takeWhile)"
}

private[transducers] final class TakeRightTransducer[A](n: Int) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new TakeRightReducer[A, R](rf, n)

  override def toString: String = s"(takeRight $n)"
}

private[transducers] final class TakeNthTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new TakeNthReducer[A, R](rf, n)

  override def toString: String = s"(takeNth $n)"
}

private[transducers] final class DropTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new DropReducer[A, R](rf, n)

  override def toString: String = s"(drop $n)"
}

private[transducers] final class DropWhileTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new DropWhileReducer[A, R](rf, f)

  override def toString: String = "(dropWhile)"
}

private[transducers] final class DropRightTransducer[A](n: Int) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new DropRightReducer[A, R](rf, n)

  override def toString: String = s"(dropRight $n)"
}

private[transducers] final class DropNthTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new DropNthReducer[A, R](rf, n)

  override def toString: String = s"(dropNth $n)"
}

private[transducers] final class DistinctTransducer[A] extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new DistinctReducer[A, R](rf)

  override def toString: String = "(distinct)"
}

private[transducers] final class ZipWithIndexTransducer[A] extends Transducer[A, (A, Int)] {
  def apply[R](rf: Reducer[(A, Int), R]): Reducer[A, R] =
    new ZipWithIndexReducer[A, R](rf)

  override def toString: String = "(zipWithIndex)"
}

private[transducers] final class GroupedTransducer[A, F[_]: AsTarget](n: Int) extends Transducer[A, F[A]] {
  def apply[R](rf: Reducer[F[A], R]): Reducer[A, R] =
    new GroupedReducer[A, R, F](rf, n)

  override def toString: String = s"(grouped $n)"
}

private[transducers] final class GroupByTransducer[A, B <: AnyRef, F[_]: AsTarget](f: A ⇒ B) extends Transducer[A, F[A]] {
  def apply[R](rf: Reducer[F[A], R]): Reducer[A, R] =
    new GroupByReducer[A, B, R, F](rf, f)

  override def toString: String = "(groupBy)"
}
