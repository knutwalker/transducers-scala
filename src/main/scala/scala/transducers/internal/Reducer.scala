package scala.transducers
package internal

import scala.language.higherKinds

private[transducers] object Reducers {

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

  abstract class Buffer[A, R, F[_]](rf: Reducer[F[A], R])(implicit F: AsTarget[F], S: Sized[F]) extends Reducer[A, R] {
    private var buffer = F.empty[A]

    final def apply(r: R) =
      rf(if (S.nonEmpty(buffer)) {
        val r2 = rf(r, buffer, new Reduced)
        buffer = F.empty[A]
        r2
      }
      else r)

    protected final def append(a: A): Unit =
      buffer = F.append(buffer, a)

    protected final def size: Long =
      S.size(buffer)

    protected final def flush(r: R, s: Reduced): R = {
      val ret = rf(r, buffer, s)
      buffer = F.empty[A]
      ret
    }
  }

}
