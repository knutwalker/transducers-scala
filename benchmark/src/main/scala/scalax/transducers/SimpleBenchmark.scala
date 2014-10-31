package scalax.transducers

import java.lang.{Iterable => JIterable}
import java.util
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

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
class SimpleBenchmark {

  import scalax.transducers.SimpleBenchmark.{IntList, JavaCollections, ScalaCollections, TransducerJava, TransducerScala}

  @Benchmark
  def javaList(bh: Blackhole, ints: IntList, f: JavaCollections): Unit = {
    bh.consume(f.f(ints.jxs))
  }

  @Benchmark
  def scalaList(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.f(ints.xs))
  }

  @Benchmark
  def scalaListWithBreakout(bh: Blackhole, ints: IntList, f: ScalaCollections): Unit = {
    bh.consume(f.fBreakout(ints.xs))
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


object SimpleBenchmark extends JTransducersConversions {

  @State(Scope.Benchmark)
  class IntList {
    val xs = (1 to 1e7.toInt).toList
    val jxs = xs.asJava
  }

  @State(Scope.Benchmark)
  class JavaCollections {
    val f: (util.List[Int]) => util.List[Int] =
      xs => xs.stream().map[Int]((_: Int) + 1).collect(Collectors.toList[Int])
  }

  @State(Scope.Benchmark)
  class ScalaCollections {
    val f: (List[Int]) => Vector[Int] = xs => xs.map(_ + 1).toVector
    val fBreakout: (List[Int]) => Vector[Int] = xs => xs.map(_ + 1)(collection.breakOut)
  }

  @State(Scope.Benchmark)
  class TransducerScala {
    val f: (List[Int]) => util.List[Int] =
      into[util.List].from[List].run(map((_: Int) + 1))
  }

  @State(Scope.Benchmark)
  class TransducerJava {
    val f: (JIterable[Int]) => util.List[Int] =
      xs => Fns.into(Fns.map((_: Int) + 1), new util.ArrayList[Int], xs)
  }

}
