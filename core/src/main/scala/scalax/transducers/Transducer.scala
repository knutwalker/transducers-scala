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
package scalax
package transducers

import scala.language.{ existentials, higherKinds }
import scala.reflect.ClassTag
import scalax.transducers.internal.CombinedTransducer

trait Transducer[@specialized(Int, Long, Double, Char, Boolean) A, @specialized(Int, Long, Double, Char, Boolean) B] {
  self ⇒

  def apply[R](rf: Reducer[B, R]): Reducer[A, R]

  def >>[C](that: Transducer[B, C]): Transducer[A, C] =
    new CombinedTransducer(self, that)

  def andThen[C](that: Transducer[B, C]): Transducer[A, C] =
    >>[C](that)

  def compose[C](that: Transducer[C, A]): Transducer[C, B] =
    new CombinedTransducer(that, self)

  def filter(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.filter[B](f)

  def filterNot(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.filterNot[B](f)

  def map[C](f: B ⇒ C): Transducer[A, C] =
    this >> transducers.map[B, C](f)

  def collect[C](pf: PartialFunction[B, C]): Transducer[A, C] =
    this >> transducers.collect[B, C](pf)

  def foreach(f: B ⇒ Unit): Transducer[A, Unit] =
    this >> transducers.foreach[B](f)

  def flatMap[C, F[_]: AsSource](f: B ⇒ F[C]): Transducer[A, C] =
    this >> transducers.flatMap[B, C, F](f)

  def take(n: Long): Transducer[A, B] =
    this >> transducers.take[B](n)

  def takeWhile(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.takeWhile[B](f)

  def takeRight(n: Int)(implicit ct: ClassTag[B]): Transducer[A, B] =
    this >> transducers.takeRight[B](n)

  def takeNth(n: Long): Transducer[A, B] =
    this >> transducers.takeNth[B](n)

  def drop(n: Long): Transducer[A, B] =
    this >> transducers.drop[B](n)

  def dropWhile(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.dropWhile[B](f)

  def dropRight(n: Int)(implicit ct: ClassTag[B]): Transducer[A, B] =
    this >> transducers.dropRight[B](n)

  def dropNth(n: Long): Transducer[A, B] =
    this >> transducers.dropNth[B](n)

  def distinct: Transducer[A, B] =
    this >> transducers.distinct[B]

  def zipWithIndex: Transducer[A, (B, Int)] =
    this >> transducers.zipWithIndex[B]

  def grouped[F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[B]] =
    this >> transducers.grouped[B, F](n)

  def partition[F[_]](f: B ⇒ C forSome { type C <: AnyRef })(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[B]] =
    this >> transducers.partition(f)
}
