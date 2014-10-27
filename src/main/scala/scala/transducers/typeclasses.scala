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
package scala.transducers

import java.util.{ Iterator ⇒ JIterator }
import java.util

import scala.language.{ higherKinds, implicitConversions }

trait AsTarget[F[_]] {
  def empty[A]: F[A]

  def append[A](fa: F[A], a: A): F[A]

  def reducer[A]: Reducer[A, F[A]] =
    internal.Reducers[A, F[A]]((as, a, _) ⇒ append(as, a))
}

trait AsSource[F[_]] {
  def hasNext[A](fa: F[A]): Boolean

  def produceNext[A](fa: F[A]): (A, F[A])
}

trait Sized[F[_]] {
  def size(f: F[_]): Int

  def isEmpty(f: F[_]): Boolean = size(f) == 0

  def nonEmpty(f: F[_]): Boolean = !isEmpty(f)
}

trait AsTargetInstances {
  implicit val list: AsTarget[List] = new AsTarget[List] {
    def empty[A] = Nil

    def append[A](fa: List[A], a: A) = fa :+ a
  }
  implicit val vector: AsTarget[Vector] = new AsTarget[Vector] {
    def empty[A] = Vector.empty

    def append[A](fa: Vector[A], a: A) = fa :+ a
  }
  implicit val stream: AsTarget[Stream] = new AsTarget[Stream] {
    def empty[A] = Stream.empty

    def append[A](fa: Stream[A], a: A) = fa :+ a
  }
  implicit val option: AsTarget[Option] = new AsTarget[Option] {
    def empty[A] = None

    def append[A](fa: Option[A], a: A) = fa orElse Some(a)
  }
  implicit val set: AsTarget[Set] = new AsTarget[Set] {
    def empty[A] = Set.empty

    def append[A](fa: Set[A], a: A) = fa + a
  }
  implicit val iterator: AsTarget[Iterator] = new AsTarget[Iterator] {
    def empty[A] = Iterator.empty

    def append[A](fa: Iterator[A], a: A) = fa ++ List(a)
  }
  implicit val iterable: AsTarget[Iterable] = new AsTarget[Iterable] {
    def empty[A] = Iterable.empty

    def append[A](fa: Iterable[A], a: A) = fa ++ List(a)
  }
  implicit val javaList: AsTarget[util.List] = new AsTarget[util.List] {
    def empty[A] = new util.ArrayList[A]
    def append[A](fa: util.List[A], a: A) = { fa.add(a); fa }
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

trait SizedInstances {
  implicit val list: Sized[List] = new Sized[List] {
    def size(f: List[_]) = f.size

    override def isEmpty(f: List[_]) = f.isEmpty
  }
  implicit val vector: Sized[Vector] = new Sized[Vector] {
    def size(f: Vector[_]) = f.size
  }
  implicit val stream: Sized[Stream] = new Sized[Stream] {
    def size(f: Stream[_]) = f.size

    override def isEmpty(f: Stream[_]) = f.isEmpty
  }
  implicit val option: Sized[Option] = new Sized[Option] {
    def size(f: Option[_]) = if (f.isEmpty) 0 else 1
  }
  implicit val set: Sized[Set] = new Sized[Set] {
    def size(f: Set[_]) = f.size

    override def isEmpty(f: Set[_]) = f.isEmpty
  }
  implicit val iterator: Sized[Iterator] = new Sized[Iterator] {
    def size(f: Iterator[_]) = f.size

    override def isEmpty(f: Iterator[_]) = f.isEmpty
  }
  implicit val iterable: Sized[Iterable] = new Sized[Iterable] {
    def size(f: Iterable[_]) = f.size
  }
}

object AsTarget extends AsTargetInstances {
  @inline def apply[F[_]](implicit F: AsTarget[F]): AsTarget[F] = F
}

object AsSource extends AsSourceInstances {
  @inline def apply[F[_]](implicit F: AsSource[F]): AsSource[F] = F
}

object Sized extends SizedInstances {
  @inline def apply[F[_]](implicit F: Sized[F]): Sized[F] = F
}
