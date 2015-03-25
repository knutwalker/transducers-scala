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

import scalax.transducers.TransducerCore

import org.reactivestreams.{Publisher, Subscriber}

import scala.language.implicitConversions

trait ReactiveStreamsSupport {

  implicit final def reactiveStreams[A](underlying: Publisher[A]): TransducerEnabledPublisher[A] =
    new TransducerEnabledPublisher[A](underlying)

  final class TransducerEnabledPublisher[A](upstream: Publisher[A]) {
    def transduce[B](transducer: TransducerCore[A, B]): Publisher[B] =
      new TransducedPublisher(upstream, transducer)
  }

  private final class TransducedPublisher[A, B](upstream: Publisher[A], transducer: TransducerCore[A, B]) extends Publisher[B] {
    def subscribe(downstream: Subscriber[_ >: B]): Unit = {
      val state = new PublisherState[A, B](downstream)
      val reducer = transducer(state.reducer)

      upstream.subscribe(state.subscriber(reducer))
      downstream.onSubscribe(state.subscription(reducer))
    }
  }

}

object ReactiveStreamsSupport extends ReactiveStreamsSupport
