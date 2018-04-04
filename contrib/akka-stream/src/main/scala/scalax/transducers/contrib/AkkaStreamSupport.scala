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

package scalax.transducers.contrib

import scalax.transducers.TransducerCore

import akka.stream.scaladsl.{ FlowOps , Flow, Source }
import akka.stream.javadsl.{ Flow ⇒ JFlow , Source ⇒ JSource }

trait AkkaStreamSupport {

  implicit final def akkaStream[A, M](underlying: Source[A, M]): TransducerEnabledStream[A, M, ({type λ[α] = Source[α, M]})#λ] =
    new TransducerEnabledStream[A, M, ({type λ[α] = Source[α, M]})#λ](underlying)

  implicit final def akkaStream[A, I, M](underlying: Flow[I, A, M]): TransducerEnabledStream[A, M, ({type λ[α] = Flow[I, α, M]})#λ] =
    new TransducerEnabledStream[A, M, ({type λ[α] = Flow[I, α, M]})#λ](underlying)

  implicit final def akkaStream[A, M](underlying: JSource[A, M]): TransducerEnabledStream[A, M, ({type λ[α] = JSource[α, M]})#λ] =
    new TransducerEnabledStream[A, M, ({type λ[α] = JSource[α, M]})#λ](underlying.asScala)

  implicit final def akkaStream[A, I, M](underlying: JFlow[I, A, M]): TransducerEnabledStream[A, M, ({type λ[α] = JFlow[I, α, M]})#λ] =
    new TransducerEnabledStream[A, M, ({type λ[α] = JFlow[I, α, M]})#λ](underlying.asScala)

  final class TransducerEnabledStream[A, M, Repr[_]] private[AkkaStreamSupport] (upstream: FlowOps[A, M]) {
    def transduce[B](transducer: TransducerCore[A, B]): Repr[B] = {
      upstream.via(new TransducerStage[A, B](transducer)).asInstanceOf[Repr[B]]
    }
  }
}

object AkkaStreamSupport extends AkkaStreamSupport
