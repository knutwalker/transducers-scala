package scala.transducer.internal

import java.util.concurrent.atomic.AtomicBoolean

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.transducer.{ AsSource, AsTarget, Reducer ⇒ ReduceFn, Sized }

private[internal] final class FilterReducer[A, R](rf: ReduceFn[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"filter: a = [$a] r = [$r]")
    if (f(a)) rf(r, a, s) else r
  }
}

private[internal] final class FilterNotReducer[A, R](rf: ReduceFn[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"filterNot: a = [$a] r = [$r]")
    if (!f(a)) rf(r, a, s) else r
  }
}

private[internal] final class MapReducer[B, A, R](rf: ReduceFn[B, R], f: A ⇒ B) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"map: a = [$a] r = [$r]")
    rf(r, f(a), s)
  }
}

private[internal] final class CollectReducer[A, B, R](rf: ReduceFn[B, R], pf: PartialFunction[A, B]) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"collect: a = [$a] r = [$r]")
    if (pf.isDefinedAt(a))
      rf(r, pf(a), s)
    else
      r
  }
}

private[internal] final class ForeachReducer[A, R](rf: ReduceFn[Unit, R], f: A ⇒ Unit) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"foreach: a = [$a] r = [$r]")
    f(a)
    r
  }
}

private[internal] final class FlatMapReducer[A, B, R, F[_]: AsSource](rf: ReduceFn[B, R], f: A ⇒ F[B]) extends Reducers.Delegate[A, R](rf) {
  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"flatMap: a = [$a] r = [$r]")
    Reducers.reduceStep(rf, r, f(a), s)
  }
}

private[internal] final class TakeReducer[A, R](rf: ReduceFn[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var taken = 1L

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"take: a = [$a] taken = [$taken] r = [$r]")
    if (taken < n) {
      taken += 1
      rf(r, a, s)
    }
    else if (taken == n) {
      taken += 1
      val res = rf(r, a, s)
      s.set(true)
      res
    }
    else r
  }
}

private[internal] final class TakeWhileReducer[A, R](rf: ReduceFn[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"takeWhile: a = [$a] r = [$r]")
    if (f(a)) rf(r, a, s)
    else {
      s.set(true)
      r
    }
  }
}

private[internal] final class TakeNthReducer[A, R](rf: ReduceFn[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var nth = 0L

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"takeNth: a = [$a] nth = [$nth] r = [$r]")
    val res = if (nth % n == 0) rf(r, a, s) else r
    nth += 1
    res
  }
}

private[internal] final class TakeRightReducer[A: ClassTag, R](rf: ReduceFn[A, R], n: Int) extends ReduceFn[A, R] {
  private val queue = new CappedEvictingQueue[A](n)

  def apply(r: R, a: A, s: AtomicBoolean) =
    { queue.add(a); r }

  def apply(r: R) =
    Reducers.reduce(rf, r, queue.elements, new AtomicBoolean)
}

private[internal] final class DropReducer[A, R](rf: ReduceFn[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var dropped = 0L

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"drop: a = [$a] dropped = [$dropped] r = [$r]")
    if (dropped < n) {
      dropped += 1
      r
    }
    else rf(r, a, s)
  }
}

private[internal] final class DropWhileReducer[A, R](rf: ReduceFn[A, R], f: A ⇒ Boolean) extends Reducers.Delegate[A, R](rf) {
  private var drop = true

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"dropWhile: a = [$a] drop = [$drop] r = [$r]")
    if (drop && f(a)) r
    else {
      drop = false
      rf(r, a, s)
    }
  }
}

private[internal] final class DropNthReducer[A, R](rf: ReduceFn[A, R], n: Long) extends Reducers.Delegate[A, R](rf) {
  private var nth = 0L

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"dropNth: a = [$a] nth = [$nth] r = [$r]")
    val res = if (nth % n == 0) r else rf(r, a, s)
    nth += 1
    res
  }
}

private[internal] final class DropRightReducer[A: ClassTag, R](rf: ReduceFn[A, R], n: Int) extends Reducers.Delegate[A, R](rf) {
  private val queue = new CappedEvictingQueue[A](n)

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"dropRight: a = [$a] queue = [$queue] r = [$r]")
    queue.add(a) match {
      case Some(oldest) ⇒ rf(r, oldest, s)
      case None         ⇒ r
    }
  }
}

private[internal] final class DistinctReducer[A, R](rf: ReduceFn[A, R]) extends Reducers.Delegate[A, R](rf) {
  private var previous: A = null.asInstanceOf[A]

  def apply(r: R, a: A, s: AtomicBoolean) = {
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

private[internal] final class BufferReducer[A, R, F[_]](rf: ReduceFn[F[A], R], n: Int)(implicit F: AsTarget[F], S: Sized[F]) extends ReduceFn[A, R] {
  private var buffer = F.empty[A]

  def apply(r: R, a: A, s: AtomicBoolean) = {
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
      val r2 = rf(r, buffer, new AtomicBoolean())
      buffer = F.empty[A]
      r2
    }
    else r
    rf(ret)
  }
}

private[internal] final class PartitionReducer[A, B <: AnyRef, R, F[_]](rf: ReduceFn[F[A], R], f: A ⇒ B)(implicit F: AsTarget[F], S: Sized[F]) extends ReduceFn[A, R] {
  private var buffer = F.empty[A]
  private val mark = new AnyRef
  private var previous = mark

  def apply(r: R, a: A, s: AtomicBoolean) = {
    //    println(s"partition: a = [$a] buffer = [$buffer] r = [$r]")
    val key = f(a)
    val ret = if ((previous eq mark) || (previous == key)) {
      buffer = F.append(buffer, a)
      r
    }
    else {
      val r2 = rf(r, buffer, s)
      buffer = F.empty[A]
      if (!s.get()) {
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
      val r2 = rf(r, buffer, new AtomicBoolean())
      buffer = F.empty[A]
      r2
    }
    else r
    rf(ret)
  }
}
