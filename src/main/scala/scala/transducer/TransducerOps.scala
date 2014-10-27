package scala.transducer

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.transducer.internal._

private[transducer] trait TransducerOps {

  def filter[A](f: A ⇒ Boolean): Transducer[A, A] =
    new FilterTransducer[A](f)

  def filterNot[A](f: A ⇒ Boolean): Transducer[A, A] =
    new FilterNotTransducer[A](f)

  def map[A, B](f: A ⇒ B): Transducer[A, B] =
    new MapTransducer[A, B](f)

  def collect[A, B](pf: PartialFunction[A, B]): Transducer[A, B] =
    new CollectTransducer[A, B](pf)

  def foreach[A](f: A ⇒ Unit): Transducer[A, Unit] =
    new ForeachTransducer[A](f)

  def flatMap[A, B, F[_]: AsSource](f: A ⇒ F[B]): Transducer[A, B] =
    new FlatMapTransducer[A, B, F](f)

  def take[A](n: Long): Transducer[A, A] =
    new TakeTransducer[A](n)

  def takeWhile[A](f: A ⇒ Boolean): Transducer[A, A] =
    new TakeWhileTransducer[A](f)

  def takeRight[A: ClassTag](n: Int): Transducer[A, A] =
    new TakeRightTransducer[A](n)

  def takeNth[A](n: Long): Transducer[A, A] =
    new TakeNthTransducer(n)

  def drop[A](n: Long): Transducer[A, A] =
    new DropTransducer[A](n)

  def dropWhile[A](f: A ⇒ Boolean): Transducer[A, A] =
    new DropWhileTransducer[A](f)

  def dropRight[A: ClassTag](n: Int): Transducer[A, A] =
    new DropRightTransducer[A](n)

  def dropNth[A](n: Long): Transducer[A, A] =
    new DropNthTransducer[A](n)

  def distinct[A]: Transducer[A, A] =
    new DistinctTransducer[A]

  def buffer[A, F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[A]] =
    new BufferTransducer[A, F](n)

  def partition[A, B <: AnyRef, F[_]](f: A ⇒ B)(implicit F: AsTarget[F], S: Sized[F]): Transducer[A, F[A]] =
    new PartitionTransducer[A, B, F](f)
}
