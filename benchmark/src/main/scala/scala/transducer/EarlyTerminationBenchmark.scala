package scala.transducer

import java.lang.{Iterable => JIterable}
import java.util
import java.util.concurrent.TimeUnit

import com.cognitect.transducers.Fns
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import scala.collection.JavaConverters._

@Threads(value = 1)
@Fork(value = 1)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class EarlyTerminationBenchmark {

  import scala.transducer.EarlyTerminationBenchmark.{IntList, ScalaCollections, TransducerJava, TransducerScala}

  @Benchmark
  def scalaList(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.f(ints.xs))
  }

  @Benchmark
  def scalaListAsVector(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.fAsVector(ints.xs))
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

object EarlyTerminationBenchmark {

  import scala.transducer.JTransducersConversions._

  @State(Scope.Benchmark)
  class IntList {
    val xs = (1 to 1e7.toInt).toList
    val jxs = xs.asJava
  }

  @State(Scope.Benchmark)
  class ScalaCollections {
    val f: (List[Int]) => Vector[Int] =
      xs => xs.map(_ + 1).map(_ + 1).map(_ + 1).take(3).toVector
    val fAsVector: (List[Int]) => Vector[Int] =
      xs => xs.toVector.map(_ + 1).map(_ + 1).map(_ + 1).take(3)
    val fAsStream: (List[Int]) => Vector[Int] =
      xs => xs.toStream.map(_ + 1).map(_ + 1).map(_ + 1).take(3).toVector
  }

  @State(Scope.Benchmark)
  class TransducerScala {
    val f: (List[Int]) => Vector[Int] =
      into[Vector].from[List].run(map((_: Int) + 1).map(_ + 1).map(_ + 1).take(3)) _
  }

  @State(Scope.Benchmark)
  class TransducerJava {
    val f: (JIterable[Int]) => util.List[Int] =
      xs => {
        val t = Fns.map((_: Int) + 1).comp(Fns.map((_: Int) + 1)).comp(Fns.map((_: Int) + 1)).comp(Fns.take(3))
        Fns.into(t, new util.ArrayList[Int], xs)
      }
  }

}
