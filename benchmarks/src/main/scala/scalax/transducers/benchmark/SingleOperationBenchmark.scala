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

import transducers.Reducer
import transducers.benchmark.SingleOperationBenchmark.BlackholeReducer
import transducers.internal.Reduced

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit


@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
class SingleOperationBenchmark {

  private[this] final val reduced = new Reduced

  private[this] final var identity: Reducer[Int, Unit] = _
  private[this] final var mapIdentity: Reducer[Int, Unit] = _
  private[this] final var filterIdentity: Reducer[Int, Unit] = _
  private[this] final var flatMapIdentity: Reducer[Int, Unit] = _
  private[this] final var takeIdentity: Reducer[Int, Unit] = _
  private[this] final var scanIdentity: Reducer[Int, Unit] = _
  private[this] final var orElseIdentity: Reducer[Int, Unit] = _

  @Setup(Level.Iteration)
  def setup(bh: Blackhole): Unit = {
    identity = transducers.noop[Int](new BlackholeReducer(bh))
    mapIdentity = transducers.map((x: Int) ⇒ x)(new BlackholeReducer(bh))
    filterIdentity = transducers.filter((x: Int) ⇒ true)(new BlackholeReducer(bh))
    flatMapIdentity = transducers.flatMap((x: Int) ⇒ List(x)).apply(new BlackholeReducer(bh))
    takeIdentity = transducers.take(Long.MaxValue)(new BlackholeReducer(bh))
    scanIdentity = transducers.scan(0)((x, y: Int) ⇒ x)(new BlackholeReducer(bh))
    orElseIdentity = transducers.orElse(-1)(new BlackholeReducer(bh))
  }

  @Benchmark
  def _00_baseline(bh: Blackhole): Unit = bh.consume(42)

  @Benchmark
  def _01_noop(bh: Blackhole): Unit = bh.consume(identity.apply((), 42, reduced))

  @Benchmark
  def _02_map(bh: Blackhole): Unit = bh.consume(mapIdentity.apply((), 42, reduced))

  @Benchmark
  def _03_filter(bh: Blackhole): Unit = bh.consume(filterIdentity.apply((), 42, reduced))

  @Benchmark
  def _04_flatMap(bh: Blackhole): Unit = bh.consume(flatMapIdentity.apply((), 42, reduced))

  @Benchmark
  def _05_take(bh: Blackhole): Unit = bh.consume(takeIdentity.apply((), 42, reduced))

  @Benchmark
  def _06_scan(bh: Blackhole): Unit = bh.consume(scanIdentity.apply((), 42, reduced))

  @Benchmark
  def _07_orElse(bh: Blackhole): Unit = bh.consume(orElseIdentity.apply((), 42, reduced))
}

object SingleOperationBenchmark {

  class BlackholeReducer(bh: Blackhole) extends Reducer[Int, Unit] {
    def apply(r: Unit, a: Int, s: Reduced): Unit = bh.consume(a)
    def apply(r: Unit): Unit = ()
    def prepare(r: Unit, s: Reduced): Unit = ()
  }
}
