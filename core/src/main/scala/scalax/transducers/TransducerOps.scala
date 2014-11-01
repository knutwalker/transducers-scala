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
package scalax.transducers

import scala.language.higherKinds
import scala.reflect.ClassTag
import scalax.transducers.internal._

private[transducers] trait TransducerOps {

  def empty[A]: Transducer[A, A] =
    new EmptyTransducer[A]

  def filter[A](f: A ⇒ Boolean): Transducer[A, A] =
    new FilterTransducer[A](f)

  def filterNot[A](f: A ⇒ Boolean): Transducer[A, A] =
    new FilterNotTransducer[A](f)

  def map[A, B](f: A ⇒ B): Transducer[A, B] =
    new MapTransducer[A, B](f)

  def collect[A, B](pf: PartialFunction[A, B]): Transducer[A, B] =
    new CollectTransducer[A, B](pf)

  def foreach[A](f: A ⇒ Unit): Transducer[A, Unit] =
    new ForeachTransducer[A](f)

  def flatMap[A, B, F[_]: AsSource](f: A ⇒ F[B]): Transducer[A, B] =
    new FlatMapTransducer[A, B, F](f)

  def fold[A, B](z: B)(f: (B, A) ⇒ B): Transducer[A, B] =
    new FoldTransducer[A, B](z, f)

  def take[A](n: Long): Transducer[A, A] =
    new TakeTransducer[A](n)

  def takeWhile[A](f: A ⇒ Boolean): Transducer[A, A] =
    new TakeWhileTransducer[A](f)

  def takeRight[A: ClassTag](n: Int): Transducer[A, A] =
    new TakeRightTransducer[A](n)

  def takeNth[A](n: Long): Transducer[A, A] =
    new TakeNthTransducer(n)

  def drop[A](n: Long): Transducer[A, A] =
    new DropTransducer[A](n)

  def dropWhile[A](f: A ⇒ Boolean): Transducer[A, A] =
    new DropWhileTransducer[A](f)

  def dropRight[A: ClassTag](n: Int): Transducer[A, A] =
    new DropRightTransducer[A](n)

  def dropNth[A](n: Long): Transducer[A, A] =
    new DropNthTransducer[A](n)

  def slice[A](from: Long, until: Long): Transducer[A, A] = {
    val lower = scala.math.max(from, 0L)
    if (until <= lower) empty[A]
    else drop[A](lower) >> take[A](until - lower)
  }

  def distinct[A]: Transducer[A, A] =
    new DistinctTransducer[A]

  def zipWithIndex[A]: Transducer[A, (A, Int)] =
    new ZipWithIndexTransducer[A]

  def grouped[A, F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[A]] =
    new GroupedTransducer[A, F](n)

  def groupBy[A, B <: AnyRef, F[_]](f: A ⇒ B)(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[A]] =
    new GroupByTransducer[A, B, F](f)
}
