package scala.transducer.internal

import java.util.concurrent.atomic.AtomicBoolean

import scala.annotation.tailrec
import scala.language.higherKinds
import scala.transducer.{AsSource, Reducer => ReduceFn}

private[transducer] object Reducer {

  def apply[A, R](f: (R, A, AtomicBoolean) => R): ReduceFn[A, R] =
    new SimpleReducer[A, R](f)

  def reduce[A, R, F[_] : AsSource](f: ReduceFn[A, R], result: R, input: F[A], reduced: AtomicBoolean): R =
    runReduce(f, f, result, input, reduced)

  def reduceStep[A, R, F[_] : AsSource](f: ReduceFn[A, R], result: R, input: F[A], reduced: AtomicBoolean): R =
    runReduce(f, identity[R], result, input, reduced)

  private def runReduce[A, R, F[_]](f: ReduceFn[A, R], g: (R => R), result: R, input: F[A], reduced: AtomicBoolean)(implicit F: AsSource[F]): R = {
    @tailrec
    def go(xs: F[A], r: R): R =
      if (reduced.get() || !F.hasNext(xs)) g(r)
      else {
        val (head, tail) = F.produceNext(xs)
        val res = f(r, head, reduced)
        go(tail, res)
      }
    go(input, result)
  }

  final class SimpleReducer[A, R](f: (R, A, AtomicBoolean) => R) extends ReduceFn[A, R] {
    def apply(r: R) = r

    def apply(r: R, a: A, s: AtomicBoolean) = f(r, a, s)
  }

  abstract class Delegate[A, R](rf: ReduceFn[_, R]) extends ReduceFn[A, R] {
    final def apply(r: R) = rf(r)
  }

}
