package scala.transducer.internal

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.transducer.{ AsSource, AsTarget, Reducer, Sized }

private[internal] final class FilterReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"filter: a = [$a] r = [$r]")
    if (f(a)) rf(r, a, s) else r
  }
}

private[internal] final class FilterNotReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"filterNot: a = [$a] r = [$r]")
    if (!f(a)) rf(r, a, s) else r
  }
}

private[internal] final class MapReducer[B, A, R](rf: Reducer[B, R], f: A ⇒ B) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"map: a = [$a] r = [$r]")
    rf(r, f(a), s)
  }
}

private[internal] final class CollectReducer[A, B, R](rf: Reducer[B, R], pf: PartialFunction[A, B]) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"collect: a = [$a] r = [$r]")
    if (pf.isDefinedAt(a))
      rf(r, pf(a), s)
    else
      r
  }
}

private[internal] final class ForeachReducer[A, R](rf: Reducer[Unit, R], f: A ⇒ Unit) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"foreach: a = [$a] r = [$r]")
    f(a)
    r
  }
}

private[internal] final class FlatMapReducer[A, B, R, F[_]: AsSource](rf: Reducer[B, R], f: A ⇒ F[B]) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"flatMap: a = [$a] r = [$r]")
    Reducers.reduceStep(rf, r, f(a), s)
  }
}

private[internal] final class TakeReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var taken = 1L

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"take: a = [$a] taken = [$taken] r = [$r]")
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
}

private[internal] final class TakeWhileReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"takeWhile: a = [$a] r = [$r]")
    if (f(a)) rf(r, a, s) else s(r)
  }
}

private[internal] final class TakeNthReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var nth = 0L

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"takeNth: a = [$a] nth = [$nth] r = [$r]")
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
    //    println(s"drop: a = [$a] dropped = [$dropped] r = [$r]")
    if (dropped < n) {
      dropped += 1
      r
    }
    else rf(r, a, s)
  }
}

private[internal] final class DropWhileReducer[A, R](rf: Reducer[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  private var drop = true

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"dropWhile: a = [$a] drop = [$drop] r = [$r]")
    if (drop && f(a)) r
    else {
      drop = false
      rf(r, a, s)
    }
  }
}

private[internal] final class DropNthReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var nth = 0L

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"dropNth: a = [$a] nth = [$nth] r = [$r]")
    val res = if (nth % n == 0) r else rf(r, a, s)
    nth += 1
    res
  }
}

private[internal] final class DropRightReducer[A: ClassTag, R](rf: Reducer[A, R], n: Int) extends Reducers.Delegate[A, R](rf) {
  private val queue = new CappedEvictingQueue[A](n)

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"dropRight: a = [$a] queue = [$queue] r = [$r]")
    queue.add(a) match {
      case Some(oldest) ⇒ rf(r, oldest, s)
      case None         ⇒ r
    }
  }
}

private[internal] final class DistinctReducer[A, R](rf: Reducer[A, R]) extends Reducers.Delegate[A, R](rf) {
  private var previous: A = null.asInstanceOf[A]

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"unique: a = [$a] previous = [$previous] r = [$r]")
    if (a != previous) {
      previous = a
      rf(r, a, s)
    }
    else {
      r
    }
  }
}

private[internal] final class GroupedReducer[A, R, F[_]](rf: Reducer[F[A], R], n: Int)(implicit F: AsTarget[F], S: Sized[F]) extends Reducer[A, R] {
  private var buffer = F.empty[A]

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"buffer: a = [$a] buffer = [$buffer] r = [$r]")
    buffer = F.append(buffer, a)
    if (S.size(buffer) == n) {
      val ret = rf(r, buffer, s)
      buffer = F.empty[A]
      ret
    }
    else r
  }

  def apply(r: R) = {
    //    println(s"buffer final: buffer = [$buffer] r = [$r]")
    val ret = if (S.nonEmpty(buffer)) {
      val r2 = rf(r, buffer, new Reduced)
      buffer = F.empty[A]
      r2
    }
    else r
    rf(ret)
  }
}

private[internal] final class PartitionReducer[A, B <: AnyRef, R, F[_]](rf: Reducer[F[A], R], f: A ⇒ B)(implicit F: AsTarget[F], S: Sized[F]) extends Reducer[A, R] {
  private var buffer = F.empty[A]
  private val mark = new AnyRef
  private var previous = mark

  def apply(r: R, a: A, s: Reduced) = {
    //    println(s"partition: a = [$a] buffer = [$buffer] r = [$r]")
    val key = f(a)
    val ret = if ((previous eq mark) || (previous == key)) {
      buffer = F.append(buffer, a)
      r
    }
    else {
      val r2 = rf(r, buffer, s)
      buffer = F.empty[A]
      if (!s.?) {
        buffer = F.append(buffer, a)
      }
      r2
    }
    previous = key
    ret
  }

  def apply(r: R) = {
    //    println(s"partition final: buffer = [$buffer] r = [$r]")
    val ret = if (S.nonEmpty(buffer)) {
      val r2 = rf(r, buffer, new Reduced)
      buffer = F.empty[A]
      r2
    }
    else r
    rf(ret)
  }
}
