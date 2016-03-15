/*
 * Copyright 2014 – 2016 Paul Horn
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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Keep, Flow, Sink, Source }
import akka.stream.javadsl.{ Keep ⇒ JKeep, Flow ⇒ JFlow, Sink ⇒ JSink, Source ⇒ JSource }

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._


final class AkkaStreamSpec extends Specification with AkkaStreamSupport with ContribTransducer with AfterAll {

  implicit val system = ActorSystem("foo")
  implicit val mat    = ActorMaterializer()

  val infiniteSource =
    Source.fromIterator(() ⇒ Iterator.from(0))

  val foldSink = Flow[(Char, Int)]
    .fold(Vector.empty[(Char, Int)])(_ :+ _)
    .map(_.toList)
    .toMat(Sink.head)(Keep.right)


  "The akka stream support" should {
    tag("contrib")
    "transduce on a Source" in { implicit ee: ExecutionEnv ⇒

      val source = infiniteSource.transduce(testTx)
      val result = source.runWith(foldSink)

      result must beTypedEqualTo(expectedResult).await
    }

    "transduce on a Flow" in { implicit ee: ExecutionEnv ⇒

      val flow = Flow[Int].transduce(testTx)
      val result = infiniteSource.via(flow).runWith(foldSink)

      result must beTypedEqualTo(expectedResult).await
    }
  }

  def afterAll(): Unit = {
    Await.result(system.terminate(), 10.seconds)
    ()
  }
}
