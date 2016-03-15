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

import scalax.transducers.{AsTarget, Transducer}

import rx.functions.Func1

import java.lang.{Boolean ⇒ JBool}
import java.util.{List ⇒ JList}
import java.util.concurrent.TimeUnit
import java.util.function.{Function ⇒ JFun, Predicate ⇒ JPred}
import java.util.stream.Collectors

import fj.data.{List ⇒ FJList}
import org.openjdk.jmh.annotations._

import scala.collection.immutable.List

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

  private[this] final val JFun1: JFun[Int, Int] = new JFun[Int, Int] {
    def apply(t: Int): Int = t * 2
  }
  private[this] final val JFun2: JFun[Int, Int] = new JFun[Int, Int] {
    def apply(t: Int): Int = t - 42
  }
  private[this] final val JPred: JPred[Int] = new JPred[Int] {
    def test(t: Int): Boolean = t % 4 == 0
  }

  private[this] final val OFunc1: Func1[Int, Int] = new Func1[Int, Int] {
    def call(t1: Int): Int = t1 * 2
  }
  private[this] final val OFunc2: Func1[Int, Int] = new Func1[Int, Int] {
    def call(t1: Int): Int = t1 - 42
  }
  private[this] final val OPred: Func1[Int, JBool] = new Func1[Int, JBool] {
    def call(t1: Int): JBool = t1 % 4 == 0
  }

  private[this] final val listAppend: AsTarget[List] = new AsTarget[List] {
    type RB[A] = List[A]

    def empty[A]: RB[A] = List.empty[A]
    def from[A](as: List[A]): RB[A] = as

    def append[A](fa: RB[A], a: A): RB[A] = fa :+ a
    def finish[A](fa: RB[A]): List[A] = fa

    def size(fa: RB[_]): Int = fa.size
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
  def bench_02_functionaljavaList(input: Input): FJList[Int] = {
    input.fjxs
      .map(FJFun1)
      .filter(FJPred)
      .map(FJFun2)
      .take(50)
  }

  @Benchmark
  def bench_03_oldTransducers(input: Input): List[Int] = {
    transducers.into[List](listAppend).run(STransducer)(input.xs)
  }

  @Benchmark
  def bench_04_newTransducers(input: Input): List[Int] = {
    transducers.into[List](AsTarget.list).run(STransducer)(input.xs)
  }

  @Benchmark
  def bench_05_javaStreams(input: Input): JList[Int] = {
    input.jxs.stream()
      .map[Int](JFun1)
      .filter(JPred)
      .map[Int](JFun2)
      .limit(50)
      .collect(Collectors.toList[Int])
  }

  @Benchmark
  def bench_06_rxJava(input: Input): JList[Int] = {
    rx.Observable.from(input.jxs)
      .map[Int](OFunc1)
      .filter(OPred)
      .map[Int](OFunc2)
      .take(50)
      .toList
      .toBlocking
      .first()
  }
}
