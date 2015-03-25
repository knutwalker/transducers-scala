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

import java.lang.{Boolean ⇒ JBool}
import java.util.concurrent.TimeUnit

import fj.data.{List ⇒ FJList}
import org.openjdk.jmh.annotations._

import scalax.transducers.{AsTarget, Transducer}

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
class ListBenchmark {

  private[this] final val ScalaFun1: Int ⇒ Int = _ * 2
  private[this] final val ScalaFun2: Int ⇒ Int = _ - 42
  private[this] final val ScalaPred: Int ⇒ Boolean = _ % 4 == 0
  private[this] final val STransducer: Transducer[Int, Int] =
    transducers.map(ScalaFun1).filter(ScalaPred).map(ScalaFun2).take(50)

  private[this] final val FJFun1: fj.F[Int, Int] = new fj.F[Int, Int] {
    def f(a: Int): Int = a * 2
  }
  private[this] final val FJFun2: fj.F[Int, Int] = new fj.F[Int, Int] {
    def f(a: Int): Int = a - 42
  }
  private[this] final val FJPred: fj.F[Int, JBool] = new fj.F[Int, JBool] {
    def f(a: Int): JBool = a % 4 == 0
  }


  @Benchmark
  def bench_01_scalaCollections(input: Input): List[Int] = {
    input.xs.map(ScalaFun1).filter(ScalaPred).map(ScalaFun2).take(50)
  }

  @Benchmark
  def bench_02_functionaljavaList(input: Input): FJList[Int] = {
    input.fjxs.map(FJFun1).filter(FJPred).map(FJFun2).take(50)
  }

  @Benchmark
  def bench_03_oldTransducers(input: Input): List[Int] = {
    transducers.into[List](AsTarget.listAppend).run(STransducer)(input.xs)
  }

  @Benchmark
  def bench_04_newTransducers(input: Input): List[Int] = {
    transducers.into[List](AsTarget.list).run(STransducer)(input.xs)
  }
}
