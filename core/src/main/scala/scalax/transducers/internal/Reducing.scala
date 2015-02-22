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

package scalax.transducers
package internal

import scala.language.higherKinds

private[transducers] object Reducing {

  def reduce[A, R, F[_]: AsSource](result: R, input: F[A])(f: Reducer[A, R]): R =
    reduceFurther(f, result, input, new Reduced)

  def reduceFurther[A, R, F[_]: AsSource](f: Reducer[A, R], result: R, input: F[A], reduced: Reduced): R =
    runReduce(f, f, result, input, reduced)

  def reduceStep[A, R, F[_]: AsSource](f: Reducer[A, R], result: R, input: F[A], reduced: Reduced): R =
    runReduce(f, identity[R], result, input, reduced)

  private def runReduce[A, R, F[_]](f: Reducer[A, R], g: (R ⇒ R), result: R, input: F[A], reduced: Reduced)(implicit F: AsSource[F]): R = {
    var acc = f.prepare(result, reduced)
    var these = input
    while (F.hasNext(these) && !reduced.?) {
      val (head, tail) = F.produceNext(these)
      acc = f(acc, head, reduced)
      these = tail
    }
    g(acc)
  }
}
