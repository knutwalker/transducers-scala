package scala

import java.util.concurrent.atomic.AtomicBoolean

import scala.language.higherKinds
import scala.transducer.internal.Reducers

package object transducer extends TransducerOps {

  def run[A, B, F[_]](xf: Transducer[B, A])(xs: F[A])(implicit F: AsSource[F], x: AsTarget[F]): F[B] =
    into[F].run(xf)(xs)

  def addto[G[_]: AsSource, A, F[_]: AsTarget, B](init: F[B])(xf: Transducer[B, A])(xs: G[A]): F[B] =
    transduceInit(init, xf, xs)

  def into[F[_]: AsTarget]: Into[F] = new Into[F]

  private[transducer] def transduceEmpty[G[_]: AsSource, A, F[_]: AsTarget, B](xf: Transducer[B, A], xs: G[A]): F[B] = {
    transduceInit(AsTarget[F].empty[B], xf, xs)
  }

  private[transducer] def transduceInit[G[_]: AsSource, A, F[_]: AsTarget, B](init: F[B], xf: Transducer[B, A], xs: G[A]): F[B] = {
    val F = AsTarget[F]
    transduce(init, xs)(xf, Reducers[B, F[B]]((bs, b, _) ⇒ F.append(bs, b)))
  }

  private def transduce[A, B, R, F[_]: AsSource](init: R, xs: F[A])(xf: Transducer[B, A], rf: Reducer[B, R]): R = {
    val xf1 = xf(rf)
    Reducers.reduce(xf1, init, xs, new AtomicBoolean)
  }
}
