package scala.transducer

import scala.language.{higherKinds, implicitConversions}

// scalaz.ApplicativePlus
trait AsTarget[F[_]] {
  def empty[A]: F[A]
  def append[A](fa: F[A], a: A): F[A]
}

// scalaz.Foldable
trait AsSource[F[_]] {
  def foldLeft[A, B](fa: F[A], z: B)(f: (B, A) => B): B
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
}

trait AsSourceInstances {
  implicit val list: AsSource[List] = new AsSource[List] {
    def foldLeft[A, B](fa: List[A], z: B)(f: (B, A) => B) =
      fa.foldLeft(z)(f)
  }
  implicit val vector: AsSource[Vector] = new AsSource[Vector] {
    def foldLeft[A, B](fa: Vector[A], z: B)(f: (B, A) => B) =
      fa.foldLeft(z)(f)
  }
  implicit val stream: AsSource[Stream] = new AsSource[Stream] {
    def foldLeft[A, B](fa: Stream[A], z: B)(f: (B, A) => B) =
      fa.foldLeft(z)(f)
  }
  implicit val option: AsSource[Option] = new AsSource[Option] {
    def foldLeft[A, B](fa: Option[A], z: B)(f: (B, A) => B) =
      fa.fold(z)(a => f(z, a))
  }
  implicit val set: AsSource[Set] = new AsSource[Set] {
    def foldLeft[A, B](fa: Set[A], z: B)(f: (B, A) => B) =
      fa.foldLeft(z)(f)
  }
}

object AsTarget extends AsTargetInstances
object AsSource extends AsSourceInstances
