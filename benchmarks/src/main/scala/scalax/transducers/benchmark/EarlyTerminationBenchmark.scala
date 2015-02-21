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

package scalax
package transducers.benchmark

import com.cognitect.transducers.Fns
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import scala.collection.JavaConverters._
import java.lang.{Iterable ⇒ JIterable}
import java.util
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class EarlyTerminationBenchmark {
  import EarlyTerminationBenchmark._

  @Benchmark
  def javaList(bh: Blackhole, ints: IntList, f: JavaCollections): Unit = {
    bh.consume(f.f(ints.jxs))
  }

  @Benchmark
  def scalaList(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.f(ints.xs))
  }

  @Benchmark
  def scalaListAsVector(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.fAsVector(ints.xs))
  }

  @Benchmark
  def scalaListAsView(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.fAsView(ints.xs))
  }

  @Benchmark
  def scalaListAsStream(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.fAsStream(ints.xs))
  }

  @Benchmark
  def javaTransducers(bh: Blackhole, ints: IntList, f: TransducerJava): Unit = {
    bh.consume(f.f(ints.jxs))
  }

  @Benchmark
  def scalaTransducers(bh: Blackhole, ints: IntList, f: TransducerScala): Unit = {
    bh.consume(f.f(ints.xs))
  }
}

object EarlyTerminationBenchmark extends JTransducersConversions {

  @State(Scope.Benchmark)
  class IntList {
    val xs = (1 to 1e7.toInt).toList
    val jxs = xs.asJava
  }

  @State(Scope.Benchmark)
  class JavaCollections {
    val f: (util.List[Int]) ⇒ util.List[Int] =
      xs ⇒ xs.stream()
        .map[Int]((_: Int) + 1)
        .map[Int]((_: Int) + 1)
        .map[Int]((_: Int) + 1)
        .limit(3)
        .collect(Collectors.toList[Int])
  }

  @State(Scope.Benchmark)
  class ScalaCollections {
    val f: (List[Int]) ⇒ Vector[Int] =
      xs ⇒ xs.map(_ + 1).map(_ + 1).map(_ + 1).take(3).toVector
    val fAsVector: (List[Int]) ⇒ Vector[Int] =
      xs ⇒ xs.toVector.map(_ + 1).map(_ + 1).map(_ + 1).take(3)
    val fAsView: (List[Int]) ⇒ Vector[Int] =
      xs ⇒ xs.view.map(_ + 1).map(_ + 1).map(_ + 1).take(3).toVector
    val fAsStream: (List[Int]) ⇒ Vector[Int] =
      xs ⇒ xs.toStream.map(_ + 1).map(_ + 1).map(_ + 1).take(3).toVector
  }

  @State(Scope.Benchmark)
  class TransducerScala {
    val f: (List[Int]) ⇒ util.List[Int] =
      scalax.transducers.into[util.List].from[List].run(transducers.map((_: Int) + 1).map(_ + 1).map(_ + 1).take(3))
  }

  @State(Scope.Benchmark)
  class TransducerJava {
    val f: (JIterable[Int]) ⇒ util.List[Int] =
      xs ⇒ {
        val t = Fns.map((_: Int) + 1).comp(Fns.map((_: Int) + 1)).comp(Fns.map((_: Int) + 1)).comp(Fns.take(3))
        Fns.into(t, new util.ArrayList[Int], xs)
      }
  }

}
