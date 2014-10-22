package scala.transducer

import scala.language.higherKinds
import scalaz.{ApplicativePlus, Foldable}

trait Transducer[@specialized(Int, Long, Double, Char, Boolean) A, @specialized(Int, Long, Double, Char, Boolean) B] { left =>

  def apply[R](rf: Reducer[A, R]): Reducer[B, R]

  def >>[C](right: Transducer[C, A]): Transducer[C, B] =
    comp(right)

  def comp[C](right: Transducer[C, A]): Transducer[C, B] =
    new Transducer[C, B] {
      def apply[R](rf: Reducer[C, R]) = left(right(rf))
    }

  def map[C](f: A => C): Transducer[C, B] =
    this >> transducer.map(f)

  def filter(f: A => Boolean): Transducer[A, B] =
    this >> transducer.filter(f)

  def flatMap[C, F[_] : Foldable](f: A => F[C]): Transducer[C, B] =
    this >> transducer.flatMap(f)

  def take(n: Long): Transducer[A, B] =
    this >> transducer.take(n)

  def drop(n: Long): Transducer[A, B] =
    this >> transducer.drop(n)
}
