package scala

import java.util.concurrent.atomic.AtomicBoolean

import scala.language.higherKinds


package object transducer {

  type Reducer[A, R] = (R, A, AtomicBoolean) => R

  // ** operations ** //

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


  // ** running them ** //

  def run[A, B, F[_]](xs: F[A], xf: Transducer[B, A])(implicit F: AsSource[F], x: AsTarget[F]): F[B] =
    into[F].run(xs, xf)

  def into[F[_] : AsTarget] = new Into[F]

  final class Into[F[_]](implicit F: AsTarget[F]) {
    def run[A, B, G[_] : AsSource](xs: G[A], xf: Transducer[B, A]): F[B] =
      transduce(F.empty[B], xs)(xf, (bs, b: B, _) => F.append(bs, b))
  }


  private def transduce[A, B, R, F[_] : AsSource](init: R, xs: F[A])(xf: Transducer[B, A], rf: Reducer[B, R]): R = {
    val xf1 = xf(rf)
    val reduced = new AtomicBoolean
    reduce(xf1, init, xs, reduced)
  }

  private def reduce[A, R, F[_]](f: Reducer[A, R], result: R, input: F[A], reduced: AtomicBoolean)(implicit F: AsSource[F]): R =
    F.foldLeft(input, result)((b, a) => {
      val r = f(b, a, reduced)
      if (reduced.get()) {
        return r
      }
      r
    })


  private final class MapTransducer[B, A](f: A => B) extends Transducer[B, A] {
    def apply[R](rf: Reducer[B, R]) =
      (r, a, s) => rf(r, f(a), s)
  }

  private final class FilterTransducer[A](f: A => Boolean) extends Transducer[A, A] {
    def apply[R](rf: Reducer[A, R]) =
      (r, a, s) => if (f(a)) rf(r, a, s) else r
  }

  private final class FlatMapTransducer[A, B, F[_] : AsSource](f: A => F[B]) extends Transducer[B, A] {
    def apply[R](rf: Reducer[B, R]) =
      (r, a, s) => reduce(rf, r, f(a), s)
  }

  private final class TakeTransducer[A](n: Long) extends Transducer[A, A] {
    def apply[R](rf: Reducer[A, R]) = new Reducer[A, R] {
      private final var taken = 1L

      def apply(r: R, a: A, s: AtomicBoolean) =
        if (taken < n) {
          taken += 1
          rf(r, a, s)
        } else if (taken == n) {
          taken += 1
          val res = rf(r, a, s)
          s.set(true)
          res
        } else {
          r
        }
    }
  }

  private final class DropTransducer[A](n: Long) extends Transducer[A, A] {
    def apply[R](rf: Reducer[A, R]) = new Reducer[A, R] {
      private final var dropped = 0L

      def apply(r: R, a: A, s: AtomicBoolean) =
        if (dropped < n) {
          dropped += 1
          r
        } else {
          rf(r, a, s)
        }
    }
  }
}
