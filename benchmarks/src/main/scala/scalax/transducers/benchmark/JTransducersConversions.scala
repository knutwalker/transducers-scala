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

package scalax.transducers.benchmark

import com.cognitect.transducers.IStepFunction

import java.util.concurrent.atomic.AtomicBoolean
import java.util.function

trait JTransducersConversions {
  implicit def fn2fjf[A, B](g: A ⇒ B): fj.F[A, B] =
    new fj.F[A, B] {
      def f(a: A): B = g(a)
    }

  implicit def fn2cognFn[A, B](f: A ⇒ B): com.cognitect.transducers.Function[A, B] =
    new com.cognitect.transducers.Function[A, B] {
      def apply(t: A) = f(t)
    }

  implicit def fn2cognStep[A, B](f: (A, B) ⇒ A): IStepFunction[A, B] =
    new IStepFunction[A, B] {
      def apply(result: A, input: B, reduced: AtomicBoolean) = f(result, input)
    }

  implicit def fn2jfn[A, B](f: A ⇒ B): function.Function[A, B] =
    new function.Function[A, B] {
      def apply(t: A) = f(t)
    }
}
