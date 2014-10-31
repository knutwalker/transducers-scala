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
package scala.transducers.contrib

import rx.lang.scala.JavaConversions.toJavaOperator
import rx.lang.scala.Observable

import scala.language.implicitConversions
import scala.transducers.Transducer

trait RxSupport {

  final class TransducerEnabledObservable[A](upstream: Observable[A]) {
    def transduce[B](transducer: Transducer[A, B]): Observable[B] = {
      upstream.lift(new OperatorTransducer(transducer))
    }
  }

  final class TransducerEnabledJavaObservable[A](upstream: rx.Observable[A]) {
    def transduce[B](transducer: Transducer[A, B]): rx.Observable[B] = {
      upstream.lift(toJavaOperator(new OperatorTransducer(transducer)))
    }
  }

  implicit final def rxJava[A](underlying: rx.Observable[A]): TransducerEnabledJavaObservable[A] =
    new TransducerEnabledJavaObservable[A](underlying)

  implicit final def rxScala[A](underlying: Observable[A]): TransducerEnabledObservable[A] =
    new TransducerEnabledObservable[A](underlying)
}
