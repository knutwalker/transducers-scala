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

import scalax.transducers.ContribTransducer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }

import org.reactivestreams.Publisher
import org.specs2.mutable.Specification

import scala.concurrent.Await
import scala.concurrent.duration.Duration

final class ReactiveStreamsSpec extends Specification with ReactiveStreamsSupport with ContribTransducer {

  "The reactivestreams support" should {
    tag("contrib")
    "transduce on a producer" in {
      implicit val system = ActorSystem("foo")
      implicit val mat = ActorMaterializer()

      val publisher: Publisher[(Char, Int)] =
        Source.fromIterator(() ⇒ Iterator.from(0))
          .runWith(Sink.asPublisher(false))
          .transduce(testTx)

      val result: List[(Char, Int)] =
        Await.result(Source.fromPublisher(publisher).runWith(Sink.fold(List.empty[(Char, Int)])(_ :+ _)), Duration.Inf)

      system.terminate()

      result ==== expectedResult
    }
  }

}
