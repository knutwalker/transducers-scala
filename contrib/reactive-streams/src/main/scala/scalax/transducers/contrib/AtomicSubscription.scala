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

import org.reactivestreams.Subscription

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import java.util.concurrent.atomic.AtomicReference

private[contrib] class AtomicSubscription extends Subscription {
  private val underlying = new AtomicReference[Option[Subscription]](None)

  private var stash = Queue.empty[Subscription ⇒ Unit]

  def set(s: Subscription): Unit =
    if (underlying.compareAndSet(None, Some(s))) {
      unstash(stash, s)
    }

  def request(n: Long): Unit =
    doOnUnderlying(_.request(n))

  private def doOnUnderlying(f: Subscription ⇒ Unit): Unit =
    underlying.get() match {
      case Some(s) ⇒ f(s)
      case None    ⇒ stash = stash enqueue f
    }

  def cancel(): Unit =
    doOnUnderlying(_.cancel())

  @tailrec
  private def unstash(q: Queue[Subscription ⇒ Unit], s: Subscription): Unit =
    if (q.nonEmpty) {
      val (head, tail) = q.dequeue
      head(s)
      unstash(tail, s)
    }
    else {
      stash = q
    }
}
