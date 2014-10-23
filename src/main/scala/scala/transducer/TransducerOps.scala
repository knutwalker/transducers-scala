package scala.transducer

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.transducer.internal._

private[transducer] trait TransducerOps {

  def filter[A](f: A => Boolean): Transducer[A, A] =
    new FilterTransducer[A](f)

  def filterNot[A](f: A => Boolean): Transducer[A, A] =
    new FilterNotTransducer[A](f)

  def map[A, B](f: A => B): Transducer[B, A] =
    new MapTransducer[B, A](f)

  def collect[A, B](pf: PartialFunction[A, B]): Transducer[B, A] =
    new CollectTransducer[A, B](pf)

  def flatMap[A, B, F[_] : AsSource](f: A => F[B]): Transducer[B, A] =
    new FlatMapTransducer[A, B, F](f)

  def take[A](n: Long): Transducer[A, A] =
    new TakeTransducer[A](n)

  def takeWhile[A](f: A => Boolean): Transducer[A, A] =
    new TakeWhileTransducer[A](f)

  def takeNth[A](n: Long): Transducer[A, A] =
    new TakeNthTransducer(n)

  def drop[A](n: Long): Transducer[A, A] =
    new DropTransducer[A](n)

  def dropWhile[A](f: A => Boolean): Transducer[A, A] =
    new DropWhileTransducer[A](f)

  def dropRight[A: ClassTag](n: Int): Transducer[A, A] =
    new DropRightTransducer[A](n)

  def unique[A]: Transducer[A, A] =
    new UniqueTransducer[A]

  def buffer[A, F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]): Transducer[F[A], A] =
    new BufferTransducer[A, F](n)

  def partition[A, B <: AnyRef, F[_]](f: A => B)(implicit F: AsTarget[F], S: Sized[F]): Transducer[F[A], A] =
    new PartitionTransducer[A, B, F](f)
}
