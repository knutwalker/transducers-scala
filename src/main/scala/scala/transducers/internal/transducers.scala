package scala.transducers
package internal

import scala.language.higherKinds
import scala.reflect.ClassTag

private[transducers] final class FilterTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new FilterReducer[A, R](rf, f)
}

private[transducers] final class FilterNotTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new FilterNotReducer[A, R](rf, f)
}

private[transducers] final class MapTransducer[A, B](f: A ⇒ B) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]) =
    new MapReducer[B, A, R](rf, f)
}

private[transducers] final class CollectTransducer[A, B](pf: PartialFunction[A, B]) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]) =
    new CollectReducer[A, B, R](rf, pf)
}

private[transducers] final class ForeachTransducer[A](f: A ⇒ Unit) extends Transducer[A, Unit] {
  def apply[R](rf: Reducer[Unit, R]) =
    new ForeachReducer[A, R](rf, f)
}

private[transducers] final class FlatMapTransducer[A, B, F[_]: AsSource](f: A ⇒ F[B]) extends Transducer[A, B] {
  def apply[R](rf: Reducer[B, R]) =
    new FlatMapReducer[A, B, R, F](rf, f)
}

private[transducers] final class TakeTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new TakeReducer[A, R](rf, n)
}

private[transducers] final class TakeWhileTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new TakeWhileReducer[A, R](rf, f)
}

private[transducers] final class TakeRightTransducer[A: ClassTag](n: Int) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new TakeRightReducer[A, R](rf, n)
}

private[transducers] final class TakeNthTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new TakeNthReducer[A, R](rf, n)
}

private[transducers] final class DropTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DropReducer[A, R](rf, n)
}

private[transducers] final class DropWhileTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DropWhileReducer[A, R](rf, f)
}

private[transducers] final class DropRightTransducer[A: ClassTag](n: Int) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DropRightReducer[A, R](rf, n)
}

private[transducers] final class DropNthTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DropNthReducer[A, R](rf, n)
}

private[transducers] final class DistinctTransducer[A]() extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DistinctReducer[A, R](rf)
}

private[transducers] final class GroupedTransducer[A, F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]) extends Transducer[A, F[A]] {
  def apply[R](rf: Reducer[F[A], R]) =
    new GroupedReducer[A, R, F](rf, n)
}

private[transducers] final class PartitionTransducer[A, B <: AnyRef, F[_]](f: A ⇒ B)(implicit F: AsTarget[F], S: Sized[F]) extends Transducer[A, F[A]] {
  def apply[R](rf: Reducer[F[A], R]) =
    new PartitionReducer[A, B, R, F](rf, f)
}
