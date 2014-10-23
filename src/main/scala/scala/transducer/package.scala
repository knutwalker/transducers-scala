package scala

import java.util.concurrent.atomic.AtomicBoolean

import scala.language.higherKinds
import scala.transducer.internal.Reducer


package object transducer extends TransducerOps {

  def run[A, B, F[_]](xf: Transducer[B, A])(xs: F[A])(implicit F: AsSource[F], x: AsTarget[F]): F[B] =
    into[F].run(xf)(xs)

  def into[A, B, F[_], G[_]](init: F[B])(xf: Transducer[B, A])(xs: G[A])(implicit F: AsTarget[F], F2: AsSource[G]): F[B] =
    transduce(init, xs)(xf, Reducer[B, F[B]]((bs, b, _) => F.append(bs, b)))

  def into[F[_] : AsTarget]: Into[F] = new Into[F]

  final class Into[F[_]](implicit F: AsTarget[F]) {
    def run[A, B, G[_] : AsSource](xf: Transducer[B, A])(xs: G[A]): F[B] =
      transduce(F.empty[B], xs)(xf, Reducer[B, F[B]]((bs, b, _) => F.append(bs, b)))

    def from[G[_] : AsSource]: IntoFrom[F, G] = new IntoFrom[F, G]
  }

  final class IntoFrom[F[_], G[_]](implicit F: AsTarget[F], G: AsSource[G]) {
    def run[A, B](xf: Transducer[B, A])(xs: G[A]): F[B] =
      transduce(F.empty[B], xs)(xf, Reducer[B, F[B]]((bs, b, _) => F.append(bs, b)))
  }

  private def transduce[A, B, R, F[_] : AsSource](init: R, xs: F[A])(xf: Transducer[B, A], rf: Reducer[B, R]): R = {
    val xf1 = xf(rf)
    Reducer.reduce(xf1, init, xs, new AtomicBoolean)
  }
}
