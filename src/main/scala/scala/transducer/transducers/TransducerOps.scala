package scala.transducer.transducers

import scala.transducer.{AsSource, Transducer}

trait TransducerOps {

  def map[A, B](f: A => B): Transducer[B, A] =
    new MapTransducer(f)

  def filter[A](f: A => Boolean): Transducer[A, A] =
    new FilterTransducer(f)

  def flatMap[A, B, F[_] : AsSource](f: A => F[B]): Transducer[B, A] =
    new FlatMapTransducer(f)

  def take[A](n: Long): Transducer[A, A] =
    new TakeTransducer(n)

  def drop[A](n: Long): Transducer[A, A] =
    new DropTransducer(n)

}
