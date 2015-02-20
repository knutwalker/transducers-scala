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
import scalax.transducers.internal._

private[transducers] trait TransducerOps {

  final def empty[A]: Transducer[A, A] =
    new EmptyTransducer[A]

  final def orElse[A](cont: ⇒ A): Transducer[A, A] =
    new OrElseTransducer[A](cont)

  final def foreach[A](f: A ⇒ Unit): Transducer[A, Unit] =
    new ForeachTransducer[A](f)

  final def map[A, B](f: A ⇒ B): Transducer[A, B] =
    new MapTransducer[A, B](f)

  final def flatMap[A, B, F[_]: AsSource](f: A ⇒ F[B]): Transducer[A, B] =
    new FlatMapTransducer[A, B, F](f)

  final def filter[A](f: A ⇒ Boolean): Transducer[A, A] =
    new FilterTransducer[A](f)

  final def filterNot[A](f: A ⇒ Boolean): Transducer[A, A] =
    filter[A](x ⇒ !f(x))

  final def collect[A, B](pf: PartialFunction[A, B]): Transducer[A, B] =
    new CollectTransducer[A, B](pf)

  final def collectFirst[A, B](pf: PartialFunction[A, B]): Transducer[A, B] =
    collect[A, B](pf).head

  final def find[A](f: A ⇒ Boolean): Transducer[A, A] =
    filter[A](f).head

  final def forall[A](f: A ⇒ Boolean): Transducer[A, Boolean] =
    collectFirst[A, Boolean] {
      case x if !f(x) ⇒ false
    }.orElse(true)

  final def exists[A](f: A ⇒ Boolean): Transducer[A, Boolean] =
    collectFirst[A, Boolean] {
      case x if f(x) ⇒ true
    }.orElse(false)

  final def fold[A, B](z: B)(f: (B, A) ⇒ B): Transducer[A, B] =
    scan[A, B](z)(f).last

  final def scan[A, B](z: B)(f: (B, A) ⇒ B): Transducer[A, B] =
    new ScanTransducer[A, B](z, f)

  final def head[A]: Transducer[A, A] =
    take[A](1)

  final def last[A]: Transducer[A, A] =
    takeRight[A](1)

  final def init[A]: Transducer[A, A] =
    dropRight[A](1)

  final def tail[A]: Transducer[A, A] =
    drop[A](1)

  final def take[A](n: Long): Transducer[A, A] =
    new TakeTransducer[A](n)

  final def takeWhile[A](f: A ⇒ Boolean): Transducer[A, A] =
    new TakeWhileTransducer[A](f)

  final def takeRight[A](n: Int): Transducer[A, A] =
    new TakeRightTransducer[A](n)

  final def takeNth[A](n: Long): Transducer[A, A] =
    new TakeNthTransducer(n)

  final def drop[A](n: Long): Transducer[A, A] =
    new DropTransducer[A](n)

  final def dropWhile[A](f: A ⇒ Boolean): Transducer[A, A] =
    new DropWhileTransducer[A](f)

  final def dropRight[A](n: Int): Transducer[A, A] =
    new DropRightTransducer[A](n)

  final def dropNth[A](n: Long): Transducer[A, A] =
    new DropNthTransducer[A](n)

  final def slice[A](from: Long, until: Long): Transducer[A, A] = {
    val lower = scala.math.max(from, 0L)
    if (until <= lower) empty[A]
    else drop[A](lower).take(until - lower)
  }

  final def distinct[A]: Transducer[A, A] =
    new DistinctTransducer[A]

  final def zipWithIndex[A]: Transducer[A, (A, Int)] =
    new ZipWithIndexTransducer[A]

  final def grouped[A, F[_]: AsTarget](n: Int): Transducer[A, F[A]] =
    new GroupedTransducer[A, F](n)

  final def groupBy[A, B <: AnyRef, F[_]: AsTarget](f: A ⇒ B): Transducer[A, F[A]] =
    new GroupByTransducer[A, B, F](f)
}
