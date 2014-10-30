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

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ ArrayBlockingQueue, BlockingQueue }

import org.reactivestreams.{ Subscriber, Subscription }

import scala.annotation.tailrec
import scala.transducers.Reducer
import scala.transducers.internal.Reduced

class PublisherState[A, B](downstream: Subscriber[_ >: B], bufferSize: Int = 1024) {

  private val upstreamSub = new AtomicSubscription
  private val reduced = new Reduced
  private val demand = new AtomicLong
  private val inputBuffer = new ArrayBlockingQueue[A](bufferSize)
  private val outputBuffer = new ArrayBlockingQueue[B](bufferSize)

  val reducer: Reducer[B, Unit] = new Reducer[B, Unit] {
    def apply(r: Unit, b: B, s: Reduced) = sendRightValue(b)

    def apply(r: Unit) = ()
  }

  def subscriber(reducer: Reducer[A, Unit]): Subscriber[A] = new Subscriber[A] {
    def onSubscribe(s: Subscription) =
      upstreamSub.set(s)

    def onError(t: Throwable) =
      downstream.onError(t)

    def onComplete() =
      downstream.onComplete()

    def onNext(t: A) =
      safeSendLeftValue(t, reducer)
  }

  def subscription(reducer: Reducer[A, Unit]): Subscription = new Subscription {
    def request(n: Long) = {
      val outstanding =
        drainBuffers(demand.addAndGet(n), reducer)
      if (reduced.? && outputBuffer.isEmpty) {
        downstream.onComplete()
        upstreamSub.cancel()
      }
      else if (outstanding > 0) {
        upstreamSub.request(n)
      }
    }

    def cancel() = {
      reduced(())
      upstreamSub.cancel()
    }
  }

  private def sendRightValue(b: B): Unit = {
    if (demand.getAndDecrement() > 0) {
      downstream.onNext(b)
    }
    else {
      demand.incrementAndGet()
      outputBuffer.offer(b)
    }
  }

  private def sendLeftValue(a: A, reducer: Reducer[A, Unit]): Unit = {
    try {
      if (!reduced.?) {
        reducer((), a, reduced)
        if (reduced.? && outputBuffer.isEmpty) {
          downstream.onComplete()
          upstreamSub.cancel()
        }
      }
    }
    catch {
      case t: Throwable ⇒ downstream.onError(t)
    }
  }

  private def safeSendLeftValue(a: A, reducer: Reducer[A, Unit]): Unit = {
    if (demand.get() > 0) {
      sendLeftValue(a, reducer)
    }
    else {
      inputBuffer.offer(a)
    }
  }

  private def drainBuffers(requested: Long, reducer: Reducer[A, Unit]): Long = {
    val outstanding =
      drainBuffer(requested, outputBuffer, sendRightValue)
    drainBuffer(outstanding, inputBuffer, sendLeftValue((_: A), reducer))
  }

  private def drainBuffer[X](requested: Long, queue: BlockingQueue[X], sending: X ⇒ Unit): Long = {
    @tailrec
    def go(requested: Long, buffered: Int): Long =
      if (requested > 0 && buffered > 0) {
        sending(queue.take())
        go(demand.get(), queue.size())
      }
      else {
        requested
      }
    go(requested, queue.size())
  }
}
