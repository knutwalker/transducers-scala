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

package scalax
package transducers.benchmark

import scalax.transducers.Transducer

import org.openjdk.jmh.annotations._

import scala.collection.immutable.List
import java.util.concurrent.TimeUnit

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 10)
@Measurement(iterations = 20)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
class LastBenchmark {

  private[this] final val TakeRight: Transducer[Int, Int] =
    transducers.takeRight(1)

  private[this] final val Last: Transducer[Int, Int] =
    transducers.last

  @Benchmark
  def bench_01_takeRight(input: Input): List[Int] =
    transducers.into[List].run(TakeRight)(input.xs)

  @Benchmark
  def bench_02_last(input: Input): List[Int] =
    transducers.into[List].run(Last)(input.xs)
}
