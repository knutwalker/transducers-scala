package scala

import scala.language.higherKinds
import scalaz.Foldable

package object transducer {

  type Reducer[A, R] = (R, A) => (R, Boolean)

  def map[A, B](f: A => B): Transducer[B, A] =
    new MapTransducer(f)

  def filter[A](f: A => Boolean): Transducer[A, A] =
    new FilterTransducer(f)

  def flatMap[A, B, F[_] : Foldable](f: A => F[B]): Transducer[B, A] =
    new FlatMapTransducer(f)

  def take[A](n: Long): Transducer[A, A] =
    new TakeTransducer(n)

  def drop[A](n: Long): Transducer[A, A] =
    new DropTransducer(n)


  private[transducer] def transduce[A, B, R, F[_] : Foldable](init: R, xs: F[A])(xf: Transducer[B, A], rf: Reducer[B, R]): R = {
    val xf1 = xf(rf)
    reduce(xf1, init, xs)._1
  }

  private def reduce[A, R, F[_]](f: Reducer[A, R], result: R, input: F[A])(implicit F: Foldable[F]): (R, Boolean) = {
    F.foldLeft(input, result)((b, a) => {
      val rAndFinished = f(b, a)
      if (rAndFinished._2) {
        return (rAndFinished._1, true)
      }
      rAndFinished._1
    }) -> false
  }


  private final class MapTransducer[B, A](f: A => B) extends Transducer[B, A] {
    def apply[R](rf: Reducer[B, R]) =
      (r, a) => {
        println(s"in map: a = [$a], r = [$r]")
        rf(r, f(a))
      }
  }

  private final class FilterTransducer[A](f: A => Boolean) extends Transducer[A, A] {
    def apply[R](rf: Reducer[A, R]) =
      (r, a) => {
        println(s"in filter: a = [$a], r = [$r]")
        if (f(a)) rf(r, a) else (r, false)
      }
  }

  private final class FlatMapTransducer[A, B, F[_] : Foldable](f: A => F[B]) extends Transducer[B, A] {
    def apply[R](rf: Reducer[B, R]) =
      (r, a) => {
        println(s"in flatMap: a = [$a], r = [$r]")
        reduce(rf, r, f(a))
      }
  }

  private final class TakeTransducer[A](n: Long) extends Transducer[A, A] {
    def apply[R](rf: Reducer[A, R]) = new Reducer[A, R] {
      private final var taken = 1L

      def apply(r: R, a: A) = {
        println(s"in take: a = [$a] taken = [$taken] r = [$r]")
        if (taken < n) {
          taken += 1
          rf(r, a)
        } else if (taken == n) {
          taken += 1
          (rf(r, a)._1, true)
        } else {
          (r, true)
        }
      }
    }
  }

  private final class DropTransducer[A](n: Long) extends Transducer[A, A] {
    def apply[R](rf: Reducer[A, R]) = new Reducer[A, R] {
      private final var dropped = 0L

      def apply(r: R, a: A) = {
        println(s"in drop: a = [$a] dropped = [$dropped] r = [$r]")
        if (dropped < n) {
          dropped += 1
          (r, false)
        } else {
          rf(r, a)
        }
      }
    }
  }

}
