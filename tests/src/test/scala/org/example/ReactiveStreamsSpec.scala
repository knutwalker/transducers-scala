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

package org.example

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import org.reactivestreams.Publisher
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures

import scalax.transducers.ContribTransducer
import scalax.transducers.contrib.ReactiveStreamsSupport

class ReactiveStreamsSpec extends FunSuite with ReactiveStreamsSupport with ScalaFutures with ContribTransducer {

  test("transducing on a producer") {
    implicit val system = ActorSystem("foo")
    implicit val mat = FlowMaterializer()

    val publisher: Publisher[(Char, Int)] =
      Source(Iterator.from(0))
        .runWith(Sink.publisher)
        .transduce(testTx)

    val result: List[(Char, Int)] =
      Source(publisher).runWith(Sink.fold(List.empty[(Char, Int)])(_ :+ _)).futureValue

    system.shutdown()

    assert(result == expectedResult)
  }
}
