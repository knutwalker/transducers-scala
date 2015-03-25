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

package scalax.transducers.contrib

import scalax.transducers.{TransducerCore, Reducer}
import scalax.transducers.internal.Reduced

import rx.lang.scala.Subscriber

import scala.util.control.NonFatal

private[contrib] final class OperatorTransducer[A, B](transducer: TransducerCore[A, B]) extends (Subscriber[B] ⇒ Subscriber[A]) {
  def apply(downstream: Subscriber[B]): Subscriber[A] =
    new Subscriber[A] {
      private val reduced = new Reduced
      private val downstreamReducer: Reducer[B, Unit] = new Reducer[B, Unit] {
        def apply(r: Unit, a: B, s: Reduced): Unit =
          if (downstream.isUnsubscribed) s(())
          else downstream.onNext(a)
        def apply(r: Unit): Unit = ()
      }
      private val reducer = transducer(downstreamReducer)

      override def onNext(value: A): Unit =
        try {
          reducer((), value, reduced)
          if (reduced.?) {
            reducer(())
            downstream.onCompleted()
            this.unsubscribe()
          }
        }
        catch {
          case NonFatal(t) ⇒ downstream.onError(t)
        }

      override def onError(error: Throwable): Unit =
        downstream.onError(error)

      override def onCompleted(): Unit =
        downstream.onCompleted()
    }
}
