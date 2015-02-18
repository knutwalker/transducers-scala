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

import java.util
import java.util.{ Iterator ⇒ JIterator }

import scala.collection.TraversableOnce
import scala.collection.immutable.{ Stream, List }
import scala.collection.mutable.{ Builder, ListBuffer }
import scala.language.{ reflectiveCalls, higherKinds, implicitConversions }
import scalax.transducers.internal.Reduced

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

  final def reducer[A]: Reducer[A, RB[A]] = new Reducer[A, RB[A]] {
    def apply(r: RB[A]): RB[A] = r
    def apply(r: RB[A], a: A, s: Reduced): RB[A] = append(r, a)
  }
}

trait AsSource[F[_]] {
  def hasNext[A](fa: F[A]): Boolean

  def produceNext[A](fa: F[A]): (A, F[A])
}

trait AsTargetInstances {
  trait FromBuilder[F[_] <: TraversableOnce[_]] extends AsTarget[F] {
    final type RB[A] = Builder[A, F[A]]

    final def from[A](as: F[A]): RB[A] = empty[A] ++= as.toTraversable.asInstanceOf[TraversableOnce[A]]
    final def append[A](fa: RB[A], a: A): RB[A] = fa += a
    final def finish[A](fa: RB[A]): F[A] = fa.result()
    final def size(fa: RB[_]): Int = fa.result().size
  }

  implicit val list: AsTarget[List] = new AsTarget[List] {
    type RB[A] = ListBuffer[A]

    def empty[A]: RB[A] = ListBuffer.empty[A]
    def from[A](as: List[A]): RB[A] = empty[A] ++= as

    def append[A](fa: RB[A], a: A): RB[A] = fa += a
    def finish[A](fa: RB[A]): List[A] = fa.result()

    def size(fa: RB[_]): Int = fa.size
  }

  val listAppend: AsTarget[List] = new AsTarget[List] {
    type RB[A] = List[A]

    def empty[A]: RB[A] = List.empty[A]
    def from[A](as: List[A]): RB[A] = as

    def append[A](fa: RB[A], a: A): RB[A] = fa :+ a
    def finish[A](fa: RB[A]): List[A] = fa

    def size(fa: RB[_]): Int = fa.size
  }

  implicit val vector: AsTarget[Vector] = new FromBuilder[Vector] {
    def empty[A] = Vector.newBuilder[A]
  }
  implicit val stream: AsTarget[Stream] = new FromBuilder[Stream] {
    def empty[A] = Stream.newBuilder[A]
  }
  implicit val set: AsTarget[Set] = new FromBuilder[Set] {
    def empty[A] = Set.newBuilder[A]
  }
  implicit val iterator: AsTarget[Iterator] = new FromBuilder[Iterator] {
    def empty[A] = Iterator.IteratorCanBuildFrom[A].apply()
  }
  implicit val iterable: AsTarget[Iterable] = new FromBuilder[Iterable] {
    def empty[A] = Iterable.newBuilder[A]
  }

  implicit val firstOption: AsTarget[Option] = new AsTarget[Option] {
    type RB[A] = Option[A]

    def empty[A] = None
    def from[A](as: Option[A]): RB[A] = as

    def append[A](fa: Option[A], a: A): RB[A] = fa orElse Option(a)
    def finish[A](fa: RB[A]): Option[A] = fa

    def size(fa: RB[_]): Int = fa.fold(0)(_ ⇒ 1)
  }

  val lastOption: AsTarget[Option] = new AsTarget[Option] {
    type RB[A] = Option[A]

    def empty[A] = None
    def from[A](as: Option[A]): RB[A] = as

    def append[A](fa: Option[A], a: A): RB[A] = Option(a) orElse fa
    def finish[A](fa: RB[A]): Option[A] = fa

    def size(fa: RB[_]): Int = fa.fold(0)(_ ⇒ 1)
  }

  implicit val javaList: AsTarget[util.List] = new AsTarget[util.List] {
    type RB[A] = util.List[A]

    def empty[A] = new util.ArrayList[A]
    def from[A](as: util.List[A]): RB[A] = new util.ArrayList[A](as)

    def append[A](fa: RB[A], a: A): RB[A] = { fa.add(a); fa }
    def finish[A](fa: RB[A]): util.List[A] = fa

    def size(fa: RB[_]): Int = fa.size()
  }
}

trait AsSourceInstances {
  implicit val list: AsSource[List] = new AsSource[List] {
    def hasNext[A](fa: List[A]) = fa.nonEmpty
    def produceNext[A](fa: List[A]) = (fa.head, fa.tail)
  }
  implicit val vector: AsSource[Vector] = new AsSource[Vector] {
    def hasNext[A](fa: Vector[A]) = fa.nonEmpty
    def produceNext[A](fa: Vector[A]) = (fa.head, fa.tail)
  }
  implicit val stream: AsSource[Stream] = new AsSource[Stream] {
    def hasNext[A](fa: Stream[A]) = fa.nonEmpty
    def produceNext[A](fa: Stream[A]) = (fa.head, fa.tail)
  }
  implicit val option: AsSource[Option] = new AsSource[Option] {
    def hasNext[A](fa: Option[A]) = fa.nonEmpty
    def produceNext[A](fa: Option[A]) = (fa.get, None)
  }
  implicit val set: AsSource[Set] = new AsSource[Set] {
    def hasNext[A](fa: Set[A]) = fa.nonEmpty
    def produceNext[A](fa: Set[A]) = (fa.head, fa.tail)
  }
  implicit val iterator: AsSource[Iterator] = new AsSource[Iterator] {
    def hasNext[A](fa: Iterator[A]) = fa.hasNext
    def produceNext[A](fa: Iterator[A]) = (fa.next(), fa)
  }
  implicit val iterable: AsSource[Iterable] = new AsSource[Iterable] {
    def hasNext[A](fa: Iterable[A]) = fa.nonEmpty
    def produceNext[A](fa: Iterable[A]) = (fa.head, fa.tail)
  }
  implicit val javaIterator: AsSource[JIterator] = new AsSource[JIterator] {
    def hasNext[A](fa: JIterator[A]) = fa.hasNext
    def produceNext[A](fa: JIterator[A]) = (fa.next(), fa)
  }
}

object AsTarget extends AsTargetInstances {
  @inline def apply[F[_]](implicit F: AsTarget[F]): AsTarget[F] = F
}

object AsSource extends AsSourceInstances {
  @inline def apply[F[_]](implicit F: AsSource[F]): AsSource[F] = F
}