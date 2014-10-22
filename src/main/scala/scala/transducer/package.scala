package scala

import java.util.concurrent.atomic.AtomicBoolean

import scala.language.higherKinds
import scala.transducer.transducers.TransducerOps


package object transducer extends TransducerOps {

  type Reducer[A, R] = (R, A, AtomicBoolean) => R

  def run[A, B, F[_]](xf: Transducer[B, A])(xs: F[A])(implicit F: AsSource[F], x: AsTarget[F]): F[B] =
    into[F].run(xf)(xs)

  def into[A, B, F[_], G[_]](init: F[B])(xf: Transducer[B, A])(xs: G[A])(implicit F: AsTarget[F], F2: AsSource[G]): F[B] =
    transduce(init, xs)(xf, (bs, b: B, _) => F.append(bs, b))

  def into[F[_] : AsTarget]: Into[F] = new Into[F]

  final class Into[F[_]](implicit F: AsTarget[F]) {
    def run[A, B, G[_] : AsSource](xf: Transducer[B, A])(xs: G[A]): F[B] =
      transduce(F.empty[B], xs)(xf, (bs, b: B, _) => F.append(bs, b))
    def from[G[_]: AsSource]: IntoFrom[F, G] = new IntoFrom[F, G]
  }

  final class IntoFrom[F[_], G[_]](implicit F: AsTarget[F], G: AsSource[G]) {
    def run[A, B](xf: Transducer[B, A])(xs: G[A]): F[B] =
      transduce(F.empty[B], xs)(xf, (bs, b: B, _) => F.append(bs, b))
  }

  private def transduce[A, B, R, F[_] : AsSource](init: R, xs: F[A])(xf: Transducer[B, A], rf: Reducer[B, R]): R = {
    val xf1 = xf(rf)
    val reduced = new AtomicBoolean
    reduce(xf1, init, xs, reduced)
  }

  private[transducer] def reduce[A, R, F[_]](f: Reducer[A, R], result: R, input: F[A], reduced: AtomicBoolean)(implicit F: AsSource[F]): R =
    F.foldLeft(input, result)((b, a) => {
      val r = f(b, a, reduced)
      if (reduced.get()) {
        return r
      }
      r
    })
}
