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

import scalax.transducers.Reducer
import scalax.transducers.internal.Reduced

import org.reactivestreams.{Subscriber, Subscription}

import scala.annotation.tailrec
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

final class PublisherState[A, B](downstream: Subscriber[_ >: B], bufferSize: Int = 1024) {

  private[contrib] val reducer: Reducer[B, Unit] = new Reducer[B, Unit] {
    def apply(r: Unit, a: B, s: Reduced): Unit = sendRightValue(a)
    def apply(r: Unit): Unit = ()
  }
  private[this] val upstreamSub = new AtomicSubscription
  private[this] val reduced = new Reduced
  private[this] val demand = new AtomicLong
  private[this] val inputBuffer = new ArrayBlockingQueue[A](bufferSize)
  private[this] val outputBuffer = new ArrayBlockingQueue[B](bufferSize)

  def subscriber(reducer: Reducer[A, Unit]): Subscriber[A] = new Subscriber[A] {
    def onSubscribe(s: Subscription): Unit =
      upstreamSub.set(s)

    def onError(t: Throwable): Unit =
      downstream.onError(t)

    def onComplete(): Unit =
      downstream.onComplete()

    def onNext(t: A): Unit =
      safeSendLeftValue(t, reducer)
  }

  private[this] def safeSendLeftValue(a: A, reducer: Reducer[A, Unit]): Unit = {
    if (demand.get() > 0) {
      sendLeftValue(a, reducer)
    }
    else {
      inputBuffer.offer(a)
    }
  }

  def subscription(reducer: Reducer[A, Unit]): Subscription = new Subscription {
    def request(n: Long): Unit = {
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

    def cancel(): Unit = {
      reduced(())
      upstreamSub.cancel()
    }
  }

  private[this] def drainBuffers(requested: Long, reducer: Reducer[A, Unit]): Long = {
    val outstanding =
      drainBuffer(requested, outputBuffer, sendRightValue)
    drainBuffer(outstanding, inputBuffer, sendLeftValue(_: A, reducer))
  }

  private[this] def sendRightValue(b: B): Unit = {
    if (demand.getAndDecrement > 0) {
      downstream.onNext(b)
    }
    else {
      demand.incrementAndGet()
      outputBuffer.offer(b)
    }
  }

  private[this] def sendLeftValue(a: A, reducer: Reducer[A, Unit]): Unit = {
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

  private[this] def drainBuffer[X](requested: Long, queue: BlockingQueue[X], sending: X ⇒ Unit): Long = {
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
