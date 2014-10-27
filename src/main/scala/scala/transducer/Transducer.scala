package scala.transducer

import scala.language.{ existentials, higherKinds }
import scala.reflect.ClassTag

trait Transducer[@specialized(Int, Long, Double, Char, Boolean) A, @specialized(Int, Long, Double, Char, Boolean) B] {
  self ⇒

  def apply[R](rf: Reducer[B, R]): Reducer[A, R]

  def >>[C](that: Transducer[B, C]): Transducer[A, C] =
    new Transducer[A, C] {
      def apply[R](rf: Reducer[C, R]) = self(that(rf))
    }

  def andThen[C](that: Transducer[B, C]): Transducer[A, C] =
    >>[C](that)

  def compose[C](that: Transducer[C, A]): Transducer[C, B] =
    new Transducer[C, B] {
      def apply[R](rf: Reducer[B, R]) = that(self(rf))
    }

  def filter(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducer.filter[B](f)

  def filterNot(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducer.filterNot[B](f)

  def map[C](f: B ⇒ C): Transducer[A, C] =
    this >> transducer.map[B, C](f)

  def collect[C](pf: PartialFunction[B, C]): Transducer[A, C] =
    this >> transducer.collect[B, C](pf)

  def foreach(f: B ⇒ Unit): Transducer[A, Unit] =
    this >> transducer.foreach[B](f)

  def flatMap[C, F[_]: AsSource](f: B ⇒ F[C]): Transducer[A, C] =
    this >> transducer.flatMap[B, C, F](f)

  def take(n: Long): Transducer[A, B] =
    this >> transducer.take[B](n)

  def takeWhile(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducer.takeWhile[B](f)

  def takeRight(n: Int)(implicit ct: ClassTag[B]): Transducer[A, B] =
    this >> transducer.takeRight[B](n)

  def takeNth(n: Long): Transducer[A, B] =
    this >> transducer.takeNth[B](n)

  def drop(n: Long): Transducer[A, B] =
    this >> transducer.drop[B](n)

  def dropWhile(f: B ⇒ Boolean): Transducer[A, B] =
    this >> transducer.dropWhile[B](f)

  def dropRight(n: Int)(implicit ct: ClassTag[B]): Transducer[A, B] =
    this >> transducer.dropRight[B](n)

  def dropNth(n: Long): Transducer[A, B] =
    this >> transducer.dropNth[B](n)

  def distinct: Transducer[A, B] =
    this >> transducer.distinct[B]

  def buffer[F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[B]] =
    this >> transducer.buffer[B, F](n)

  def partition[F[_]](f: B ⇒ C forSome { type C <: AnyRef })(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[B]] =
    this >> transducer.partition(f)
}
