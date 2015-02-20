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

private[transducers] object Reducers {

  def apply[A, R](f: (R, A, Reduced) ⇒ R): Reducer[A, R] =
    new SimpleReducer[A, R](f)

  def reduce[A, R, F[_]: AsSource](result: R, input: F[A])(f: Reducer[A, R]): R =
    reduceFurther(f, result, input, new Reduced)

  def reduceFurther[A, R, F[_]: AsSource](f: Reducer[A, R], result: R, input: F[A], reduced: Reduced): R =
    runReduce(f, f, result, input, reduced)

  def reduceStep[A, R, F[_]: AsSource](f: Reducer[A, R], result: R, input: F[A], reduced: Reduced): R =
    runReduce(f, identity[R], result, input, reduced)

  private def runReduce[A, R, F[_]](f: Reducer[A, R], g: (R ⇒ R), result: R, input: F[A], reduced: Reduced)(implicit F: AsSource[F]): R = {
    var acc = result
    var these = input
    while (F.hasNext(these) && !reduced.?) {
      val (head, tail) = F.produceNext(these)
      acc = f(acc, head, reduced)
      these = tail
    }
    g(acc)
  }

  abstract class Delegate[A, R](rf: Reducer[_, R]) extends Reducer[A, R] {
    final def apply(r: R) = rf(r)
  }

  abstract class Buffer[A, R, F[_]](rf: Reducer[F[A], R])(implicit F: AsTarget[F]) extends Reducer[A, R] {
    private var buffer = F.empty[A]

    final def apply(r: R) =
      rf(if (F.nonEmpty(buffer)) {
        val r2 = rf(r, F.finish(buffer), new Reduced)
        buffer = F.empty[A]
        r2
      }
      else r)

    protected final def append(a: A): Unit =
      buffer = F.append(buffer, a)

    protected final def size: Int =
      F.size(buffer)

    protected final def flush(r: R, s: Reduced): R = {
      val ret = rf(r, F.finish(buffer), s)
      buffer = F.empty[A]
      ret
    }
  }

  private final class SimpleReducer[A, R](f: (R, A, Reduced) ⇒ R) extends Reducer[A, R] {
    def apply(r: R) = r

    def apply(r: R, a: A, s: Reduced) = f(r, a, s)
  }

}
