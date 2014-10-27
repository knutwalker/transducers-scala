package scala.transducers

import scala.language.higherKinds

final class Into[G[_]: AsTarget] {
  def run[A, F[_]: AsSource, B](xf: Transducer[A, B])(xs: F[A]): G[B] =
    transduceEmpty(xf)(xs)

  def from[F[_]: AsSource]: IntoFrom[F, G] = new IntoFrom[F, G]
}

final class IntoFrom[F[_]: AsSource, G[_]: AsTarget] {
  def run[A, B](xf: Transducer[A, B]): F[A] ⇒ G[B] =
    xs ⇒ transduceEmpty(xf)(xs)
}
