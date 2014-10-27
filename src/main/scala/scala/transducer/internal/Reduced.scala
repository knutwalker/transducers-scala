package scala.transducer.internal

import java.util.concurrent.atomic.AtomicBoolean

class Reduced {
  private[this] final val state = new AtomicBoolean()

  def apply[T](x: T): T = {
    state.set(true)
    x
  }

  def ? : Boolean = state.get()

  override def toString = s"Reduced(${state.get()}})"

  override def hashCode() = 31 * state.get().##

  override def equals(obj: scala.Any) = obj match {
    case r: Reduced ⇒ r.? == state.get()
    case _          ⇒ false
  }
}
