package scala.transducer

import scala.language.{ existentials, higherKinds }
import scala.reflect.ClassTag

trait Transducer[@specialized(Int, Long, Double, Char, Boolean) A, @specialized(Int, Long, Double, Char, Boolean) B] {
  left ⇒

  def apply[R](rf: Reducer[A, R]): Reducer[B, R]

  def >>[C](right: Transducer[C, A]): Transducer[C, B] =
    new Transducer[C, B] {
      def apply[R](rf: Reducer[C, R]) = left(right(rf))
    }

  def andThen[C](right: Transducer[C, A]): Transducer[C, B] =
    >>[C](right)

  def filter(f: A ⇒ Boolean): Transducer[A, B] =
    this >> transducer.filter[A](f)

  def filterNot(f: A ⇒ Boolean): Transducer[A, B] =
    this >> transducer.filterNot[A](f)

  def map[C](f: A ⇒ C): Transducer[C, B] =
    this >> transducer.map[A, C](f)

  def collect[C](pf: PartialFunction[A, C]): Transducer[C, B] =
    this >> transducer.collect(pf)

  def flatMap[C, F[_]: AsSource](f: A ⇒ F[C]): Transducer[C, B] =
    this >> transducer.flatMap[A, C, F](f)

  def take(n: Long): Transducer[A, B] =
    this >> transducer.take[A](n)

  def takeWhile(f: A ⇒ Boolean): Transducer[A, B] =
    this >> transducer.takeWhile[A](f)

  def takeNth(n: Long): Transducer[A, B] =
    this >> transducer.takeNth[A](n)

  def drop(n: Long): Transducer[A, B] =
    this >> transducer.drop[A](n)

  def dropWhile(f: A ⇒ Boolean): Transducer[A, B] =
    this >> transducer.dropWhile[A](f)

  def dropRight(n: Int)(implicit ct: ClassTag[A]): Transducer[A, B] =
    this >> transducer.dropRight[A](n)

  def distinct: Transducer[A, B] =
    this >> transducer.distinct[A]

  def buffer[F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]): Transducer[F[A], B] =
    this >> transducer.buffer[A, F](n)

  def partition[F[_]](f: A ⇒ C forSome { type C <: AnyRef })(implicit F: AsTarget[F], S: Sized[F]): Transducer[F[A], B] =
    this >> transducer.partition(f)
}
