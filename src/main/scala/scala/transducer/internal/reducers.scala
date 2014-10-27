package scala.transducer.internal

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.transducer.{ AsSource, AsTarget, Reducer, Sized }

private[internal] final class FilterReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) =
    if (f(a)) rf(r, a, s) else r
}

private[internal] final class FilterNotReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) =
    if (!f(a)) rf(r, a, s) else r
}

private[internal] final class MapReducer[B, A, R](rf: Reducer[B, R], f: A ⇒ B) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) =
    rf(r, f(a), s)
}

private[internal] final class CollectReducer[A, B, R](rf: Reducer[B, R], pf: PartialFunction[A, B]) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) =
    if (pf.isDefinedAt(a)) rf(r, pf(a), s) else r
}

private[internal] final class ForeachReducer[A, R](rf: Reducer[Unit, R], f: A ⇒ Unit) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) = {
    f(a)
    r
  }
}

private[internal] final class FlatMapReducer[A, B, R, F[_]: AsSource](rf: Reducer[B, R], f: A ⇒ F[B]) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) =
    Reducers.reduceStep(rf, r, f(a), s)
}

private[internal] final class TakeReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var taken = 1L

  def apply(r: R, a: A, s: Reduced) =
    if (taken < n) {
      taken += 1
      rf(r, a, s)
    }
    else if (taken == n) {
      taken += 1
      s(rf(r, a, s))
    }
    else r
}

private[internal] final class TakeWhileReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {

  def apply(r: R, a: A, s: Reduced) =
    if (f(a)) rf(r, a, s) else s(r)
}

private[internal] final class TakeNthReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var nth = 0L

  def apply(r: R, a: A, s: Reduced) = {
    val res = if (nth % n == 0) rf(r, a, s) else r
    nth += 1
    res
  }
}

private[internal] final class TakeRightReducer[A: ClassTag, R](rf: Reducer[A, R], n: Int) extends Reducer[A, R] {
  private val queue = new CappedEvictingQueue[A](n)

  def apply(r: R, a: A, s: Reduced) =
    { queue.add(a); r }

  def apply(r: R) =
    Reducers.reduce(rf, r, queue.elements, new Reduced)
}

private[internal] final class DropReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var dropped = 0L

  def apply(r: R, a: A, s: Reduced) = {
    if (dropped < n) {
      dropped += 1
      r
    }
    else rf(r, a, s)
  }
}

private[internal] final class DropWhileReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  private var drop = true

  def apply(r: R, a: A, s: Reduced) =
    if (drop && f(a)) r
    else {
      drop = false
      rf(r, a, s)
    }
}

private[internal] final class DropNthReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var nth = 0L

  def apply(r: R, a: A, s: Reduced) = {
    val res = if (nth % n == 0) r else rf(r, a, s)
    nth += 1
    res
  }
}

private[internal] final class DropRightReducer[A: ClassTag, R](rf: Reducer[A, R], n: Int) extends Reducers.Delegate[A, R](rf) {
  private val queue = new CappedEvictingQueue[A](n)

  def apply(r: R, a: A, s: Reduced) =
    queue.add(a) match {
      case Some(oldest) ⇒ rf(r, oldest, s)
      case None         ⇒ r
    }
}

private[internal] final class DistinctReducer[A, R](rf: Reducer[A, R]) extends Reducers.Delegate[A, R](rf) {
  private var previous: A = null.asInstanceOf[A]

  def apply(r: R, a: A, s: Reduced) =
    if (a != previous) {
      previous = a
      rf(r, a, s)
    }
    else r
}

private[internal] final class GroupedReducer[A, R, F[_]](rf: Reducer[F[A], R], n: Int)(implicit F: AsTarget[F], S: Sized[F]) extends Reducers.Buffer[A, R, F](rf) {

  def apply(r: R, a: A, s: Reduced) = {
    append(a)
    if (size == n) flush(r, s) else r
  }
}

private[internal] final class PartitionReducer[A, B <: AnyRef, R, F[_]](rf: Reducer[F[A], R], f: A ⇒ B)(implicit F: AsTarget[F], S: Sized[F]) extends Reducers.Buffer[A, R, F](rf) {
  private val mark = new AnyRef
  private var previous = mark

  def apply(r: R, a: A, s: Reduced) = {
    val key = f(a)
    val ret = if ((previous eq mark) || (previous == key)) {
      append(a)
      r
    }
    else {
      val r2 = flush(r, s)
      if (!s.?) {
        append(a)
      }
      r2
    }
    previous = key
    ret
  }
}
