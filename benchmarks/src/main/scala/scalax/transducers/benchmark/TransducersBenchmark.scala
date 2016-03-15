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

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
class TransducersBenchmark {

  private[this] final val Identity: Transducer[Int, Int] =
    transducers.noop
  private[this] final val MapIdentity: Transducer[Int, Int] =
    transducers.map(x ⇒ x)
  private[this] final val FilterIdentity: Transducer[Int, Int] =
    transducers.filter(x ⇒ true)
  private[this] final val FlatMapIdentity: Transducer[Int, Int] =
    transducers.flatMap(x ⇒ List(x))
  private[this] final val TakeIdentity: Transducer[Int, Int] =
    transducers.take(Long.MaxValue)
  private[this] final val TakeAtomIdentity: Transducer[Int, Int] =
    new TakeAtomTransducer[Int](Long.MaxValue)
  private[this] final val ScanIdentity: Transducer[Int, Int] =
    transducers.scan(0)(_ + _)
  private[this] final val ScanAtomIdentity: Transducer[Int, Int] =
    new ScanAtomTransducer[Int, Int](0, _ + _)
  private[this] final val OrElseIdentity: Transducer[Int, Int] =
    transducers.orElse(-1)
  private[this] final val OrElseAtomIdentity: Transducer[Int, Int] =
    new OrElseAtomTransducer[Int](-1)

  @Benchmark
  def bench_01_noop(input: Input): Vector[Int] = {
    transducers.into[Vector].run(Identity)(input.xs)
  }

  @Benchmark
  def bench_02_map(input: Input): Vector[Int] = {
    transducers.into[Vector].run(MapIdentity)(input.xs)
  }

  @Benchmark
  def bench_03_filter(input: Input): Vector[Int] = {
    transducers.into[Vector].run(FilterIdentity)(input.xs)
  }

  @Benchmark
  def bench_04_flatMap(input: Input): Vector[Int] = {
    transducers.into[Vector].run(FlatMapIdentity)(input.xs)
  }

  @Benchmark
  def bench_05_take(input: Input): Vector[Int] = {
    transducers.into[Vector].run(TakeIdentity)(input.xs)
  }

  @Benchmark
  def bench_06_takeAtom(input: Input): Vector[Int] = {
    transducers.into[Vector].run(TakeAtomIdentity)(input.xs)
  }

  @Benchmark
  def bench_07_scan(input: Input): Vector[Int] = {
    transducers.into[Vector].run(ScanIdentity)(input.xs)
  }

  @Benchmark
  def bench_08_scanAtom(input: Input): Vector[Int] = {
    transducers.into[Vector].run(ScanAtomIdentity)(input.xs)
  }

  @Benchmark
  def bench_09_orElse(input: Input): Vector[Int] = {
    transducers.into[Vector].run(OrElseIdentity)(input.xs)
  }

  @Benchmark
  def bench_10_orElseAtom(input: Input): Vector[Int] = {
    transducers.into[Vector].run(OrElseAtomIdentity)(input.xs)
  }
}
