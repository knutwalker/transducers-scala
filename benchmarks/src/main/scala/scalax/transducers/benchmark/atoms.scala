package scalax
package transducers.benchmark

import transducers.internal.Reduced
import transducers.{Reducer, Transducer}

import annotation.tailrec
import java.util.concurrent.atomic.{AtomicLong, AtomicBoolean, AtomicReference}


final class ScanAtomTransducer[A, B](z: B, f: (B, A) ⇒ B) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]): Reducer[A, R] =
    new ScanAtomReducer[A, B, R](rf, z, f)

  override def toString: String = "(scan-atom)"
}
final class ScanAtomReducer[A, B, R](rf: Reducer[B, R], z: B, f: (B, A) ⇒ B) extends Reducer[A, R] {
  private[this] val result = new AtomicReference[B](z)

  @tailrec
  private[this] def update(a: A): B = {
    val prev = result.get()
    val next = f(prev, a)
    if (result.compareAndSet(prev, next)) next else update(a)
  }

  override def prepare(r: R, s: Reduced): R =
    rf(r, result.get(), s)

  def apply(r: R, a: A, s: Reduced): R = {
    val res = update(a)
    rf(r, res, s)
  }

  def apply(r: R): R = rf(r)
}


final class OrElseAtomTransducer[A](cont: ⇒ A) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new OrElseAtomReducer[A, R](rf, cont)

  override def toString: String = "(orElse-atom)"
}
final class OrElseAtomReducer[A, R](rf: Reducer[A, R], cont: ⇒ A) extends Reducer[A, R] {
  private[this] val hasValue = new AtomicBoolean(false)

  def apply(r: R, a: A, s: Reduced): R = {
    hasValue.set(true)
    rf(r, a, s)
  }

  def apply(r: R): R = {
    rf(if (hasValue.get()) r else rf(r, cont, new Reduced))
  }
}


final class TakeAtomTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]): Reducer[A, R] =
    new TakeAtomReducer[A, R](rf, n)

  override def toString: String = s"(take-atom $n)"
}
final class TakeAtomReducer[A, R](rf: Reducer[A, R], n: Long) extends Reducer[A, R] {
  private[this] val taken = new AtomicLong(1L)

  def apply(r: R, a: A, s: Reduced): R =
    if (taken.get() < n) {
      taken.incrementAndGet()
      rf(r, a, s)
    }
    else {
      s(rf(r, a, s))
    }

  def apply(r: R): R = rf(r)
}