/*
 * Copyright 2015 Paul Horn
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
package scalax.transducers

import java.util.concurrent.TimeUnit

import fj.data.{ List ⇒ FJList }

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 5)
@Measurement(iterations = 7)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class ListBenchmark {

  import ListBenchmark.{ IntList, ScalaCollections, FunctionalJavaList, TransducersFast, TransducersSlow }

  @Benchmark
  def _01_scalaCollections(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.f(ints.xs))
  }

  @Benchmark
  def _02_functionaljavaList(bh: Blackhole, ints: IntList, f: FunctionalJavaList): Unit = {
    bh.consume(f.f(ints.fjxs))
  }

  @Benchmark
  def _03_oldTransducers(bh: Blackhole, ints: IntList, f: TransducersSlow): Unit = {
    bh.consume(f.f(ints.xs))
  }

  @Benchmark
  def _04_oldTransducers(bh: Blackhole, ints: IntList, f: TransducersFast): Unit = {
    bh.consume(f.f(ints.xs))
  }
}

object ListBenchmark extends JTransducersConversions {

  @State(Scope.Benchmark)
  class IntList {
    final val xs = (1 to 10000).toList
    final val fjxs = FJList.list(xs: _*)
  }

  @State(Scope.Benchmark)
  class ScalaCollections {
    final val f: (List[Int]) ⇒ List[Int] =
      _.map(_ * 2).filter(_ % 4 == 0).map(_ - 42).take(50)
  }

  @State(Scope.Benchmark)
  class FunctionalJavaList {
    final val f: (FJList[Int]) ⇒ FJList[Int] =
      _.map((_: Int) * 2)
        .filter((_: Int) % 4 == 0: java.lang.Boolean)
        .map((_: Int) - 42)
        .take(50)
  }

  @State(Scope.Benchmark)
  class TransducersSlow {
    private final val tx = map((_: Int) + 1)
      .filter(_ % 4 == 0).map(_ - 42).take(50)
    final val f: (List[Int]) ⇒ List[Int] =
      into[List](AsTarget.slowList).from[List].run(tx)
  }

  @State(Scope.Benchmark)
  class TransducersFast {
    private final val tx = map((_: Int) + 1)
      .filter(_ % 4 == 0).map(_ - 42).take(50)
    final val f: (List[Int]) ⇒ List[Int] =
      into[List](AsTarget.list).from[List].run(tx)
  }
}
