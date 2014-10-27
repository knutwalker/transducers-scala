package scala.transducer.internal

import scala.language.higherKinds
import scala.transducer.{ AsSource, Reducer }

private[transducer] object Reducers {

  def apply[A, R](f: (R, A, Reduced) ⇒ R): Reducer[A, R] =
    new SimpleReducer[A, R](f)

  def reduce[A, R, F[_]: AsSource](f: Reducer[A, R], result: R, input: F[A]): R =
    reduce(f, result, input, new Reduced)

  def reduce[A, R, F[_]: AsSource](f: Reducer[A, R], result: R, input: F[A], reduced: Reduced): R =
    runReduce(f, f, result, input, reduced)

  def reduceStep[A, R, F[_]: AsSource](f: Reducer[A, R], result: R, input: F[A], reduced: Reduced): R =
    runReduce(f, identity[R], result, input, reduced)

  private def runReduce[A, R, F[_]](f: Reducer[A, R], g: (R ⇒ R), result: R, input: F[A], reduced: Reduced)(implicit F: AsSource[F]): R = {
    var acc = result
    var these = input
    while (F.hasNext(these) && !reduced.?) {
      val (head, tail) = F.produceNext(these)
      acc = f(acc, head, reduced)
      these = tail
    }
    g(acc)
  }

  final class SimpleReducer[A, R](f: (R, A, Reduced) ⇒ R) extends Reducer[A, R] {
    def apply(r: R) = r

    def apply(r: R, a: A, s: Reduced) = f(r, a, s)
  }

  abstract class Delegate[A, R](rf: Reducer[_, R]) extends Reducer[A, R] {
    final def apply(r: R) = rf(r)
  }

}
