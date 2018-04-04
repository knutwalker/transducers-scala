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

package scalax.transducers

import scalax.transducers.internal.Reduced

import scala.annotation.implicitNotFound
import scala.collection.{TraversableOnce, mutable}
import scala.collection.immutable.{List, Stream}
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import java.lang.{Iterable ⇒ JIterable}
import java.util
import java.util.{Iterator ⇒ JIterator}

@implicitNotFound("Don't know how to transduce into a ${R}. You need to provide an implicit instance of AsTarget[${R}].")
trait AsTarget[R[_]] {
  type RB[_]

  //======= constructors
  def empty[A]: RB[A]
  def from[A](as: R[A]): RB[A]

  //======= mutators
  def append[A](fa: RB[A], a: A): RB[A]
  def finish[A](fa: RB[A]): R[A]

  //======= size
  def size(fa: RB[_]): Int
  def isEmpty(fa: RB[_]): Boolean = size(fa) == 0
  @inline final def nonEmpty(fa: RB[_]): Boolean = !isEmpty(fa)

  final def reducer[A]: Reducer[A, RB[A]] = new AsTargetReducer[A, RB[A]](this.append)
}

final class AsTargetReducer[A, R](t: (R, A) ⇒ R) extends Reducer[A, R] {
  def prepare(r: R, s: Reduced): R = r
  def apply(r: R): R = r
  def apply(r: R, a: A, s: Reduced): R = t(r, a)
}

@implicitNotFound("Don't know how to transduce from a ${F}. You need to provide an implicit instance of AsSource[${F}].")
trait AsSource[F[_]] {
  type Repr[_]

  def prepare[A](fa: F[A]): Repr[A]

  def hasNext[A](fa: Repr[A]): Boolean

  def produceCurrent[A](fa: Repr[A]): A

  def produceNext[A](fa: Repr[A]): Repr[A]
}

trait AsTargetInstances {
  trait FromBuilder[F[_] <: TraversableOnce[_]] extends AsTarget[F] {
    final type RB[A] = mutable.Builder[A, F[A]]

    final def from[A](as: F[A]): RB[A] = empty[A] ++= as.toTraversable.asInstanceOf[TraversableOnce[A]]
    final def append[A](fa: RB[A], a: A): RB[A] = fa += a
    final def finish[A](fa: RB[A]): F[A] = fa.result()
    def size(fa: RB[_]): Int = fa.result().size
  }

  implicit val list: AsTarget[List] = new FromBuilder[List] {
    def empty[A]: RB[A] = new ListBuffer[A]
    override def size(fa: RB[_]): Int = fa.asInstanceOf[ListBuffer[_]].size
  }

  implicit val vector: AsTarget[Vector] = new FromBuilder[Vector] {
    def empty[A]: RB[A] = Vector.newBuilder[A]
  }
  implicit val stream: AsTarget[Stream] = new FromBuilder[Stream] {
    def empty[A]: RB[A] = Stream.newBuilder[A]
  }
  implicit val set: AsTarget[Set] = new FromBuilder[Set] {
    def empty[A]: RB[A] = Set.newBuilder[A]
  }
  implicit val iterator: AsTarget[Iterator] = new AsTarget[Iterator] {
    type RB[A] = ArrayBuffer[A]

    def empty[A]: RB[A] = new ArrayBuffer[A]
    def from[A](as: Iterator[A]): RB[A] = empty[A] ++= as

    def append[A](fa: RB[A], a: A): RB[A] = fa += a
    def finish[A](fa: RB[A]): Iterator[A] = fa.iterator

    def size(fa: RB[_]): Int = fa.length
  }
  implicit val iterable: AsTarget[Iterable] = new FromBuilder[Iterable] {
    def empty[A]: RB[A] = new ListBuffer[A]
    override def size(fa: RB[_]): Int = fa.asInstanceOf[ListBuffer[_]].size
  }

  implicit val firstOption: AsTarget[Option] = new AsTarget[Option] {
    type RB[A] = Option[A]

    def empty[A]: RB[A] = None
    def from[A](as: Option[A]): RB[A] = as

    def append[A](fa: Option[A], a: A): RB[A] = fa orElse Option(a)
    def finish[A](fa: RB[A]): Option[A] = fa

    def size(fa: RB[_]): Int = fa.fold(0)(_ ⇒ 1)
  }

  val lastOption: AsTarget[Option] = new AsTarget[Option] {
    type RB[A] = Option[A]

    def empty[A]: RB[A] = None
    def from[A](as: Option[A]): RB[A] = as

    def append[A](fa: Option[A], a: A): RB[A] = Option(a) orElse fa
    def finish[A](fa: RB[A]): Option[A] = fa

    def size(fa: RB[_]): Int = fa.fold(0)(_ ⇒ 1)
  }

  implicit val javaList: AsTarget[util.List] = new AsTarget[util.List] {
    type RB[A] = util.List[A]

    def empty[A]: RB[A] = new util.ArrayList[A]
    def from[A](as: util.List[A]): RB[A] = new util.ArrayList[A](as)

    def append[A](fa: RB[A], a: A): RB[A] = { fa.add(a); fa }
    def finish[A](fa: RB[A]): util.List[A] = fa

    def size(fa: RB[_]): Int = fa.size()
  }
}

trait AsSourceInstances {
  class FromTraversable[F[X] <: Traversable[X]] extends AsSource[F] {
    type Repr[A] = F[A]

    def prepare[A](fa: F[A]): F[A] = fa

    def hasNext[A](fa: F[A]): Boolean = fa.nonEmpty

    def produceCurrent[A](fa: F[A]): A = fa.head

    def produceNext[A](fa: F[A]): F[A] = fa.tail.asInstanceOf[F[A]]
  }

  implicit val list: AsSource[List] = new FromTraversable[List]

  implicit val vector: AsSource[Vector] = new FromTraversable[Vector]

  implicit val stream: AsSource[Stream] = new FromTraversable[Stream]

  implicit val set: AsSource[Set] = new FromTraversable[Set]

  implicit val iterable: AsSource[Iterable] = new FromTraversable[Iterable]

  implicit val option: AsSource[Option] = new AsSource[Option] {
    type Repr[A] = Option[A]

    def prepare[A](fa: Option[A]): Option[A] = fa

    def hasNext[A](fa: Option[A]): Boolean = fa.nonEmpty

    def produceCurrent[A](fa: Option[A]): A = fa.get

    def produceNext[A](fa: Option[A]): Option[A] = None
  }

  implicit val iterator: AsSource[Iterator] = new AsSource[Iterator] {
    type Repr[A] = Iterator[A]

    def prepare[A](fa: Iterator[A]): Iterator[A] = fa

    def hasNext[A](fa: Iterator[A]): Boolean = fa.hasNext

    def produceCurrent[A](fa: Iterator[A]): A = fa.next()

    def produceNext[A](fa: Iterator[A]): Iterator[A] = fa
  }

  implicit val javaIterator: AsSource[JIterator] = new AsSource[JIterator] {
    type Repr[A] = JIterator[A]

    def prepare[A](fa: JIterator[A]): JIterator[A] = fa

    def hasNext[A](fa: JIterator[A]): Boolean = fa.hasNext

    def produceCurrent[A](fa: JIterator[A]): A = fa.next()

    def produceNext[A](fa: JIterator[A]): JIterator[A] = fa
  }

  implicit val javaIterable: AsSource[JIterable] = new AsSource[JIterable] {
    type Repr[A] = JIterator[A]

    def prepare[A](fa: JIterable[A]): JIterator[A] = fa.iterator()

    def hasNext[A](fa: JIterator[A]): Boolean = fa.hasNext

    def produceCurrent[A](fa: JIterator[A]): A = fa.next()

    def produceNext[A](fa: JIterator[A]): JIterator[A] = fa
  }

  implicit val javaList: AsSource[util.List] = new AsSource[util.List] {
    type Repr[A] = JIterator[A]

    def prepare[A](fa: util.List[A]): JIterator[A] = fa.iterator()

    def hasNext[A](fa: JIterator[A]): Boolean = fa.hasNext

    def produceCurrent[A](fa: JIterator[A]): A = fa.next()

    def produceNext[A](fa: JIterator[A]): JIterator[A] = fa
  }

  implicit val javaArray: AsSource[Array] = new AsSource[Array] {
    type Repr[A] = JIterator[A]

    def prepare[A](fa: Array[A]): JIterator[A] = util.Arrays.asList(fa: _*).iterator()

    def hasNext[A](fa: JIterator[A]): Boolean = fa.hasNext

    def produceCurrent[A](fa: JIterator[A]): A = fa.next()

    def produceNext[A](fa: JIterator[A]): JIterator[A] = fa
  }
}

object AsTarget extends AsTargetInstances {
  @inline def apply[F[_]](implicit F: AsTarget[F]): AsTarget[F] = F

  type Id[A] = A
  implicit val idAsTarget: AsTarget[Id] = new AsTarget[Id] {
    type RB[A] = A
    def empty[A]: A = null.asInstanceOf[A] // scalastyle:ignore
    def from[A](as: Id[A]): A = as
    def finish[A](fa: A): Id[A] = fa
    def size(fa: Id[_]): Int = 1
    def append[A](fa: A, a: A): A = a
  }
}

object AsSource extends AsSourceInstances {
  @inline def apply[F[_]](implicit F: AsSource[F]): AsSource[F] = F
}
