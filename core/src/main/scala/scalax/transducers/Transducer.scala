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

package scalax
package transducers

import internal.CombinedTransducer

trait Transducer[@specialized(Int, Long, Double, Char, Boolean) A, @specialized(Int, Long, Double, Char, Boolean) B] extends TransducerCore[A, B] {
  self ⇒

  private[transducers] def combineWith[C](that: Transducer[B, C]): Transducer[A, C] =
    new CombinedTransducer(self, that)

  final def andThen[C](that: Transducer[B, C]): Transducer[A, C] =
    combineWith[C](that)

  final def compose[C](that: Transducer[C, A]): Transducer[C, B] =
    that.combineWith(self)

  final def >>[C](that: Transducer[B, C]): Transducer[A, C] =
    combineWith[C](that)

  final def empty[C]: Transducer[A, C] =
    this >> transducers.empty[B, C]

  final def orElse(cont: ⇒ B): Transducer[A, B] =
    this >> transducers.orElse[B](cont)

  final def foreach(f: B ⇒ Unit): Transducer[A, Unit] =
    this >> transducers.foreach[B](f)

  final def map[C](f: B ⇒ C): Transducer[A, C] =
    this >> transducers.map[B, C](f)

  final def flatMap[C, F[_]: AsSource](f: B ⇒ F[C]): Transducer[A, C] =
    this >> transducers.flatMap[B, C, F](f)

  final def filter(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.filter[B](f)

  final def filterNot(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.filterNot[B](f)

  final def collect[C](pf: PartialFunction[B, C]): Transducer[A, C] =
    this >> transducers.collect[B, C](pf)

  final def collectFirst[C](pf: PartialFunction[B, C]): Transducer[A, C] =
    this >> transducers.collectFirst[B, C](pf)

  final def find(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.find[B](f)

  final def forall(f: B ⇒ Boolean): Transducer[A, Boolean] =
    this >> transducers.forall[B](f)

  final def exists(f: B ⇒ Boolean): Transducer[A, Boolean] =
    this >> transducers.exists[B](f)

  final def fold[C](z: C)(f: (C, B) ⇒ C): Transducer[A, C] =
    this >> transducers.fold[B, C](z)(f)

  final def scan[C](z: C)(f: (C, B) ⇒ C): Transducer[A, C] =
    this >> transducers.scan[B, C](z)(f)

  final def foldAlong[S, C](z: S)(f: (S, B) ⇒ (S, C)): Transducer[A, C] =
    this >> transducers.foldAlong[B, C, S](z)(f)

  final def head: Transducer[A, B] =
    this >> transducers.head

  final def last: Transducer[A, B] =
    this >> transducers.last

  final def init: Transducer[A, B] =
    this >> transducers.init

  final def tail: Transducer[A, B] =
    this >> transducers.tail

  final def take(n: Long): Transducer[A, B] =
    this >> transducers.take[B](n)

  final def takeWhile(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.takeWhile[B](f)

  final def takeRight(n: Int): Transducer[A, B] =
    this >> transducers.takeRight[B](n)

  final def takeNth(n: Long): Transducer[A, B] =
    this >> transducers.takeNth[B](n)

  final def drop(n: Long): Transducer[A, B] =
    this >> transducers.drop[B](n)

  final def dropWhile(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducers.dropWhile[B](f)

  final def dropRight(n: Int): Transducer[A, B] =
    this >> transducers.dropRight[B](n)

  final def dropNth(n: Long): Transducer[A, B] =
    this >> transducers.dropNth[B](n)

  final def slice(from: Long, until: Long): Transducer[A, B] =
    this >> transducers.slice[B](from, until)

  final def distinct: Transducer[A, B] =
    this >> transducers.distinct[B]

  final def zipWithIndex: Transducer[A, (B, Int)] =
    this >> transducers.zipWithIndex[B]

  final def grouped[F[_]: AsTarget](n: Int): Transducer[A, F[B]] =
    this >> transducers.grouped[B, F](n)

  final def groupBy[F[_]: AsTarget](f: B ⇒ C forSome { type C <: AnyRef }): Transducer[A, F[B]] =
    this >> transducers.groupBy(f)
}
