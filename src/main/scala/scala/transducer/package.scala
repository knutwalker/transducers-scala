package scala

import scala.language.higherKinds
import scala.transducer.internal.Reducers

package object transducer extends TransducerOps {

  def run[A, B, F[_]](xf: Transducer[A, B])(xs: F[A])(implicit F: AsSource[F], x: AsTarget[F]): F[B] =
    into[F].run(xf)(xs)

  def addto[A, F[_]: AsSource, B, G[_]: AsTarget](init: G[B])(xf: Transducer[A, B])(xs: F[A]): G[B] =
    transduceInit(xf)(init, xs)

  def into[F[_]: AsTarget]: Into[F] = new Into[F]

  private[transducer] def transduceEmpty[A, F[_]: AsSource, B, G[_]: AsTarget](xf: Transducer[A, B])(xs: F[A]): G[B] =
    transduceInit(xf)(AsTarget[G].empty[B], xs)

  private[transducer] def transduceInit[A, F[_]: AsSource, B, G[_]: AsTarget](xf: Transducer[A, B])(init: G[B], xs: F[A]): G[B] = {
    val G = AsTarget[G]
    transduce(xf)(init)(xs, G.reducer[B])
  }

  private def transduce[A, B, R, F[_]: AsSource](xf: Transducer[A, B])(init: R)(xs: F[A], rf: Reducer[B, R]): R = {
    val xf1 = xf(rf)
    Reducers.reduce(xf1, init, xs)
  }
}
