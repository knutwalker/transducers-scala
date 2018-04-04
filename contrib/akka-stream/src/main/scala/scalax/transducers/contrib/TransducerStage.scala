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

import scalax.transducers.contrib.TransducerStage.TransducerLogic
import scalax.transducers.internal.Reduced
import scalax.transducers.{ Reducer, TransducerCore }

import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import akka.stream.{ Attributes, FlowShape, Inlet, Outlet }

import scala.util.control.NonFatal


final class TransducerStage[A, B](transducer: TransducerCore[A, B]) extends GraphStage[FlowShape[A, B]] {

  val shape = FlowShape.of[A, B](Inlet("Transducer.in"), Outlet("Transducer.out"))

  def createLogic(attr: Attributes): GraphStageLogic =
    new TransducerLogic[A, B](shape, transducer)
}
object TransducerStage {
  private sealed trait NoValue
  private object NoInput extends NoValue
  private object NoOutput extends NoValue

  private final class TransducerLogic[A, B](shape: FlowShape[A, B], transducer: TransducerCore[A, B])
    extends GraphStageLogic(shape) with InHandler with OutHandler {

    private[this] val in                = shape.in
    private[this] val out               = shape.out
    private[this] val reduced           = new Reduced
    private[this] val downstreamReducer = new Reducer[B, NoValue] {
      final def prepare(r: NoValue, s: Reduced): NoValue = r
      final def apply(r: NoValue): NoValue = NoOutput
      def apply(r: NoValue, b: B, s: Reduced): NoValue = {
        if (!isClosed(out)) {
          emit(out, b)
          NoOutput
        } else {
          s(NoOutput)
        }
      }
    }
    private[this] val reducer           = transducer(downstreamReducer)

    private def inIsPullable() =
      !hasBeenPulled(in) && !isClosed(in)

    setHandler(in, this)
    setHandler(out, this)
    def onPull(): Unit = {
      assert(inIsPullable())
      pull(in)
    }
    def onPush(): Unit = {
      val element = grab(in)
      try {
        val out = reducer(NoInput, element, reduced)
        if (!reduced.?) {
          // if out is still NoInput, no element reached the downstream reducer
          // meaning the element may have been discarded or buffered.
          // we need to pull again
          if (out eq NoInput) {
            assert(inIsPullable())
            pull(in)
          }
        } else {
          completeStage()
        }
      } catch {
        case NonFatal(ex) ⇒
          fail(out, ex)
          completeStage()
      }
    }
  }
}
