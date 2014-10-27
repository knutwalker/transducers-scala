package scala.transducer

import scala.transducer.internal.Reduced

trait Reducer[A, R] extends ((R, A, Reduced) ⇒ R) with (R ⇒ R) {
  def apply(r: R, a: A, s: Reduced): R

  def apply(r: R): R
}
