/*
 * Copyright 2014 Paul Horn
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
    into[F].run(xf)(xs)

  def addto[A, F[_]: AsSource, B, G[_]: AsTarget](init: G[B])(xf: Transducer[A, B])(xs: F[A]): G[B] =
    transduceInit(xf)(init, xs)

  def into[F[_]: AsTarget]: Into[F] = new Into[F]

  private[transducers] def transduceEmpty[A, F[_]: AsSource, B, G[_]: AsTarget](xf: Transducer[A, B])(xs: F[A]): G[B] =
    transduceInit(xf)(AsTarget[G].empty[B], xs)

  private[transducers] def transduceInit[A, F[_]: AsSource, B, G[_]: AsTarget](xf: Transducer[A, B])(init: G[B], xs: F[A]): G[B] = {
    val G = AsTarget[G]
    transduce(xf)(init)(xs, G.reducer[B])
  }

  private def transduce[A, B, R, F[_]: AsSource](xf: Transducer[A, B])(init: R)(xs: F[A], rf: Reducer[B, R]): R = {
    val xf1 = xf(rf)
    internal.Reducers.reduce(xf1, init, xs)
  }
}
