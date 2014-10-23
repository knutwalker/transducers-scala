package scala.transducer

import java.util.concurrent.atomic.AtomicBoolean

trait Reducer[A, R] extends ((R, A, AtomicBoolean) ⇒ R) with (R ⇒ R) {
  def apply(r: R, a: A, s: AtomicBoolean): R

  def apply(r: R): R
}
