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
package org.example

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import org.reactivestreams.Publisher
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures

import scala.transducers.Transducer
import scala.transducers.contrib.ReactiveStreamsSupport

class ReactiveStreamsSpec extends FunSuite with ReactiveStreamsSupport with ScalaFutures {

  test("transducing on a producer") {
    implicit val system = ActorSystem("foo")
    implicit val mat = FlowMaterializer()

    val tx: Transducer[Int, (Char, Int)] = transducers
      .filter[Int](_ % 2 == 0)
      .map(x ⇒ s">$x<")
      .take(7)
      .flatMap(_.toList)
      .zipWithIndex
      .dropRight(4)

    val publisher: Publisher[(Char, Int)] =
      Source(Iterator.from(0))
        .runWith(Sink.publisher)
        .transduce(tx)

    val result: Vector[(Char, Int)] =
      Source(publisher).runWith(Sink.fold(Vector.empty[(Char, Int)])(_ :+ _)).futureValue

    system.shutdown()

    assert(result == Vector(
      '>' -> 0, '0' -> 1, '<' -> 2,
      '>' -> 3, '2' -> 4, '<' -> 5,
      '>' -> 6, '4' -> 7, '<' -> 8,
      '>' -> 9, '6' -> 10, '<' -> 11,
      '>' -> 12, '8' -> 13, '<' -> 14,
      '>' -> 15, '1' -> 16, '0' -> 17, '<' -> 18))
  }
}
