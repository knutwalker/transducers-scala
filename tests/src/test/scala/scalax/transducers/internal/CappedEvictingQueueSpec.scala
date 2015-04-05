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

package scalax.transducers.internal

import scalax.transducers.Arbitraries

import scalaz.@@

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object CappedEvictingQueueSpec extends Specification with ScalaCheck with Arbitraries {

  implicit val nonEmptyList = Arbitrary(arbitrary[List[String]] suchThat (_.nonEmpty))

  "The capped evicting queue" should {

    "consume all elements" in prop { (xs: List[String]) ⇒
      val queue = new CappedEvictingQueue[String](xs.size)
      val added = xs map queue.add
      added must contain(beNone).forall
    }

    "evict the oldest elements" in prop { (xs: List[String]) ⇒
      val queue = new CappedEvictingQueue[String](xs.size)
      xs foreach queue.add
      val evicted = xs map queue.add
      xs.map(Option(_)) ==== evicted
    }

    "iterator all elements from old to young" in prop { (xs: List[String]) ⇒
      val queue = new CappedEvictingQueue[String](xs.size)
      xs foreach queue.add
      queue.elements.toList ==== xs
    }

    "iterator only the live elements" in prop { (xs: List[String], ys: List[String]) ⇒
      val newXs = Iterator.continually(ys).flatMap(identity).take(xs.size).toList
      val queue = new CappedEvictingQueue[String](xs.size)
      xs foreach queue.add
      newXs foreach queue.add
      queue.elements.toList ==== newXs
    }

    "iterator only added elements" in prop { (xs: List[String]) ⇒
      val size = xs.size
      val max = size / 2
      val subset = xs.take(max)
      val queue = new CappedEvictingQueue[String](size)
      subset foreach queue.add
      queue.elements.toList ==== subset
    }

    "iterator knows its size" in prop { (xs: List[String]) ⇒
      val queue = new CappedEvictingQueue[String](xs.size)
      xs foreach queue.add
      val iter = queue.elements

      iter.hasDefiniteSize ==== true
      iter.size ==== xs.size
    }

    "have a size" in prop { (xs: List[String]) ⇒
      val queue = new CappedEvictingQueue[String](xs.size)
      xs foreach queue.add

      queue.size ==== xs.size
    }

    "has isEmpty" in prop { (xs: List[String]) ⇒
      val queue = new CappedEvictingQueue[String](xs.size)
      xs foreach queue.add

      queue.isEmpty ==== xs.isEmpty
    }

    "has nonEmpty" in prop { (xs: List[String]) ⇒
      val queue = new CappedEvictingQueue[String](xs.size)
      xs foreach queue.add

      queue.nonEmpty ==== xs.nonEmpty
    }

    "decline non-positive capacities" in prop { (n: Int @@ Negative) ⇒
      new CappedEvictingQueue[String](n) must throwA[IllegalArgumentException].like {
        case e ⇒ e.getMessage must startWith("requirement failed")
      }
    }

    "show current elements in toString" in prop { (xs: List[String], n: Int @@ NonZeroPositive) ⇒
      val capacity = xs.size max n
      val overCapacity = n - xs.size
      val items = if (overCapacity <= 0)
        xs.take(1).map(x ⇒ s"($x)") ::: xs.drop(1)
      else
        xs ::: "(null)" :: List.fill(overCapacity - 1)("null")

      val queue = new CappedEvictingQueue[String](capacity)
      xs foreach queue.add

      queue.toString ==== items.mkString("[", ", ", "]")
    }
  }
}
