/*
 * Copyright 2014 – 2015 Paul Horn
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

package scalax

import scala.language.higherKinds

package object transducers extends TransducerOps {

  def run[A, B, F[_]](xf: Transducer[A, B])(xs: F[A])(implicit F: AsSource[F], x: AsTarget[F]): F[B] =
    transduceFromNaught(xf)(xs)

  def run[A, B, F[_]](xs: F[A])(xf: Transducer[A, B])(implicit F: AsSource[F], x: AsTarget[F]): F[B] =
    transduceFromNaught(xf)(xs)

  def into[F[_]: AsTarget]: Into[F] = new Into[F]

  def addto[B, F[_]: AsTarget](init: F[B]): Addto[B, F] = new Addto[B, F](init)

  private[transducers] def transduceFromNaught[A, F[_], B, G[_]](xf: Transducer[A, B])(xs: F[A])(implicit F: AsSource[F], G: AsTarget[G]): G[B] =
    transduceAnything[A, F, B, G](xf)(F, G)(G.empty[B], xs)

  private[transducers] def transduceFromInit[A, F[_], B, G[_]](xf: Transducer[A, B])(init: G[B], xs: F[A])(implicit F: AsSource[F], G: AsTarget[G]): G[B] =
    transduceAnything[A, F, B, G](xf)(F, G)(G.from[B](init), xs)

  private def transduceAnything[A, F[_], B, G[_]](xf: Transducer[A, B])(F: AsSource[F], G: AsTarget[G])(builder: G.RB[B], xs: F[A]): G[B] = {
    val xf1: Reducer[A, G.RB[B]] = xf(G.reducer[B])
    val rb: G.RB[B] = internal.Reducing.reduce(builder, xs)(xf1)(F)
    G.finish(rb)
  }
}
