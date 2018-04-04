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

package scalax
package transducers.benchmark

import scalax.transducers.Transducer
import scalax.transducers.contrib.AkkaStreamSupport

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import akka.stream.{ ActorMaterializer, Materializer }

import scalaz.stream.Process

import io.iteratee.Enumeratee
import org.openjdk.jmh.annotations._
import play.api.libs.{ iteratee => p }

import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import java.util.concurrent.TimeUnit

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
class StreamsBenchmark extends AkkaStreamSupport {

  private[this] final val ScalaFun1  : Int ⇒ Int                    = _ * 2
  private[this] final val ScalaFun2  : Int ⇒ Int                    = _ - 42
  private[this] final val ScalaPred  : Int ⇒ Boolean                = _ % 4 == 0
  private[this] final val STransducer: Transducer[Int, Int]         =
    transducers.map(ScalaFun1).filter(ScalaPred).map(ScalaFun2).take(50)
  private[this]       val SFLow      : Flow[Int, Int, NotUsed]      = Flow[Int]
    .map(ScalaFun1)
    .filter(ScalaPred)
    .map(ScalaFun2)
    .take(50)
  private[this]       val SSink      : Sink[Int, Future[List[Int]]] = Flow[Int]
    .fold(ListBuffer.empty[Int])(_ += _)
    .map(_.result())
    .toMat(Sink.head)(Keep.right)
  private[this]       var system     : ActorSystem                  = _
  private[this]       var mat        : Materializer                 = _

  @Setup
  def startup(): Unit = {
    system = ActorSystem("bench")
    mat = ActorMaterializer()(system)
  }

  @TearDown
  def shutdown(): Unit = {
    mat.asInstanceOf[ActorMaterializer].shutdown()
    Await.ready(system.terminate(), Duration.Inf)
    ()
  }

  @Benchmark
  def bench_01_scalaCollections(input: Input): List[Int] = {
    input.xs
      .map(ScalaFun1)
      .filter(ScalaPred)
      .map(ScalaFun2)
      .take(50)
  }

  @Benchmark
  def bench_02_playIteratee(input: Input): List[Int] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    Await.result(
      p.Enumerator.enumerate(input.xs) &>
      p.Enumeratee.map(ScalaFun1) &>
      p.Enumeratee.filter(ScalaPred) &>
      p.Enumeratee.map(ScalaFun2) &>
      p.Enumeratee.take(50) |>>>
      p.Iteratee.getChunks,
      Duration.Inf
    )
  }

  @Benchmark
  def bench_03_scalazStream(input: Input): List[Int] = {
    Process.emitAll(input.xs)
      .map(ScalaFun1)
      .filter(ScalaPred)
      .map(ScalaFun2)
      .take(50)
      .toList
  }

  @Benchmark
  def bench_04_fs2(input: Input): List[Int] = {
    fs2.Stream.emits(input.xs)
      .map(ScalaFun1)
      .filter(ScalaPred)
      .map(ScalaFun2)
      .take(50)
      .toList
  }

  @Benchmark
  def bench_05_ioIteratee(input: Input): List[Int] = {
    io.iteratee.Enumerator.enumList(input.xs)
      .map(ScalaFun1)
      .through(Enumeratee.filter(ScalaPred))
      .map(ScalaFun2)
      .take(50)
      .toVector
      .toList
  }

  @Benchmark
  def bench_06_akkaStreamUnfused(input: Input): List[Int] = {
    Await.result(
      Source(input.xs)
        .via(SFLow)
        .runWith(SSink)(mat),
      Duration.Inf
    )
  }

  @Benchmark
  def bench_07_akkaStreamFused(input: Input): List[Int] = {
    Await.result(
      Source(input.xs)
        .map(ScalaFun1)
        .filter(ScalaPred)
        .map(ScalaFun2)
        .take(50)
        .fold(ListBuffer.empty[Int])(_ += _)
        .map(_.result())
        .runWith(Sink.head)(mat),
      Duration.Inf
    )
  }

  @Benchmark
  def bench_08_akkaStransducer(input: Input): List[Int] = {
    Await.result(
      Source(input.xs)
        .transduce(STransducer)
        .fold(ListBuffer.empty[Int])(_ += _)
        .map(_.result())
        .runWith(Sink.head)(mat),
      Duration.Inf
    )
  }

  @Benchmark
  def bench_09_transducers(input: Input): List[Int] = {
    transducers.into[List].run(STransducer)(input.xs)
  }
}
