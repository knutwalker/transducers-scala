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

import java.util
import java.util.concurrent.TimeUnit
import java.util.function.{Function ⇒ JFun, Predicate ⇒ JPred}
import java.util.stream.Collectors

import com.cognitect.transducers.{Fns, Function ⇒ CFun, ITransducer, Predicate ⇒ CPred}
import org.openjdk.jmh.annotations._

import scalax.transducers.Transducer

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
class SimpleBenchmark {

  private[this] final val ScalaFun: Int ⇒ Int = _ + 1
  private[this] final val ScalaPred: Int ⇒ Boolean = _ % 2 == 0
  private[this] final val STransducer: Transducer[Int, Int] =
    transducers.map(ScalaFun).filter(ScalaPred)

  private[this] final val JavaFun: JFun[Int, Int] = new JFun[Int, Int] {
    def apply(t: Int): Int = t + 1
  }
  private[this] final val JavaPred: JPred[Int] = new JPred[Int] {
    def test(t: Int): Boolean = t % 2 == 0
  }

  private[this] final val CogFun: CFun[Int, Int] = new CFun[Int, Int] {
    def apply(t: Int): Int = t + 1
  }
  private[this] final val CogPred: CPred[Int] = new CPred[Int] {
    def test(t: Int): Boolean = t % 2 == 0
  }
  private[this] final val JTransducer: ITransducer[Int, Int] =
    Fns.compose(Fns.map(CogFun), Fns.filter(CogPred))

  @Benchmark
  def bench_01_javaList(input: Input): util.List[Int] = {
    input.jxs.stream()
      .map[Int](JavaFun)
      .filter(JavaPred)
      .collect(Collectors.toList[Int])
  }

  @Benchmark
  def bench_02_scalaList(input: Input): Vector[Int] = {
    input.xs.map(ScalaFun).filter(ScalaPred).toVector
  }

  @Benchmark
  def bench_03_scalaListWithBreakout(input: Input): Vector[Int] = {
    val mapped: Vector[Int] = input.xs.map(ScalaFun)(scala.collection.breakOut)
    mapped.filter(ScalaPred)
  }

  @Benchmark
  def bench_04_javaTransducers(input: Input): util.List[Int] = {
    Fns.into(JTransducer, new util.ArrayList[Int], input.jxs)
  }

  @Benchmark
  def bench_05_scalaTransducers(input: Input): Vector[Int] = {
    transducers.into[Vector].run(STransducer)(input.xs)
  }

  @Benchmark
  def bench_06_scalaToJavaTransducers(input: Input): util.List[Int] = {
    transducers.into[util.List].run(STransducer)(input.xs)
  }
}
