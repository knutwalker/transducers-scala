/*
 * Copyright 2014 – 2018 Paul Horn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scalax.transducers

import scala.language.higherKinds

final class Into[G[_]: AsTarget] {
  def run[A, F[_]: AsSource, B](xf: Transducer[A, B])(xs: F[A]): G[B] =
    transduceFromNaught(xf)(xs)

  def run[A, F[_]: AsSource, B](xs: F[A])(xf: Transducer[A, B]): G[B] =
    transduceFromNaught(xf)(xs)

  def from[F[_]: AsSource]: IntoFrom[F, G] = new IntoFrom[F, G]
}

final class Addto[B, G[_]: AsTarget](init: G[B]) {
  def run[A, F[_]: AsSource](xf: Transducer[A, B])(xs: F[A]): G[B] =
    transduceFromInit(xf)(init, xs)

  def run[A, F[_]: AsSource](xs: F[A])(xf: Transducer[A, B]): G[B] =
    transduceFromInit(xf)(init, xs)

  def from[F[_]: AsSource]: AddtoFrom[B, F, G] = new AddtoFrom[B, F, G](init)
}

final class IntoFrom[F[_]: AsSource, G[_]: AsTarget] {
  def run[A, B](xf: Transducer[A, B]): F[A] ⇒ G[B] =
    xs ⇒ transduceFromNaught(xf)(xs)
}

final class AddtoFrom[B, F[_]: AsSource, G[_]: AsTarget](init: G[B]) {
  def run[A](xf: Transducer[A, B]): F[A] ⇒ G[B] =
    xs ⇒ transduceFromInit(xf)(init, xs)
}
