package scala.transducer

import scala.language.higherKinds
import scalaz.{ApplicativePlus, Foldable}

trait Transducer[@specialized(Int, Long, Double, Char, Boolean) A, @specialized(Int, Long, Double, Char, Boolean) B] { left =>
  import scala.transducer

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

object Transducer {
  def run[A, B, F[_]](xs: F[A], xf: Transducer[B, A])(implicit F: Foldable[F], x: ApplicativePlus[F]): F[B] =
    into[F].run(xs, xf)

  def into[F[_] : ApplicativePlus] = new Into[F]

  def compose[A, B, C](left: Transducer[B, C], right: Transducer[A, B]): Transducer[A, C] =
    left comp right

  final class Into[F[_]](implicit F: ApplicativePlus[F]) {
    def run[A, B, G[_] : Foldable](xs: G[A], xf: Transducer[B, A]): F[B] =
      transduce(F.empty[B], xs)(xf, (bs, b: B) => F.plus(bs, F.point(b)) -> false)
  }
}
