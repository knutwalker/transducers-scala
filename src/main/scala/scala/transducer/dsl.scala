package scala.transducer

import scala.language.higherKinds

final class Into[F[_]: AsTarget] {
  def run[G[_]: AsSource, A, B](xf: Transducer[B, A])(xs: G[A]): F[B] =
    transduceEmpty(xf, xs)

  def from[G[_]: AsSource]: IntoFrom[G, F] = new IntoFrom[G, F]
}

final class IntoFrom[G[_]: AsSource, F[_]: AsTarget] {
  def run[A, B](xf: Transducer[B, A])(xs: G[A]): F[B] =
    transduceEmpty(xf, xs)
}
