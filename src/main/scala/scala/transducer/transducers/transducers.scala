package scala.transducer.transducers

import java.util.concurrent.atomic.AtomicBoolean

import scala.transducer.{AsSource, Reducer, Transducer, reduce}


private[transducer] final class FilterTransducer[A](f: A => Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    (r, a, s) => if (f(a)) rf(r, a, s) else r
}


private[transducer] final class MapTransducer[B, A](f: A => B) extends Transducer[B, A] {
  def apply[R](rf: Reducer[B, R]) =
    (r, a, s) => rf(r, f(a), s)
}


private[transducer] final class FlatMapTransducer[A, B, F[_] : AsSource](f: A => F[B]) extends Transducer[B, A] {
  def apply[R](rf: Reducer[B, R]) =
    (r, a, s) => reduce(rf, r, f(a), s)
}


private[transducer] final class TakeTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) = new Reducer[A, R] {
    private final var taken = 1L

    def apply(r: R, a: A, s: AtomicBoolean) =
      if (taken < n) {
        taken += 1
        rf(r, a, s)
      } else if (taken == n) {
        taken += 1
        val res = rf(r, a, s)
        s.set(true)
        res
      } else {
        r
      }
  }
}


private[transducer] final class DropTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) = new Reducer[A, R] {
    private final var dropped = 0L

    def apply(r: R, a: A, s: AtomicBoolean) =
      if (dropped < n) {
        dropped += 1
        r
      } else {
        rf(r, a, s)
      }
  }
}
