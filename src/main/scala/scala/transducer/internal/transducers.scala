package scala.transducer.internal

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.transducer.{ AsSource, AsTarget, Reducer, Sized, Transducer }

private[transducer] final class FilterTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new FilterReducer[A, R](rf, f)
}

private[transducer] final class FilterNotTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new FilterNotReducer[A, R](rf, f)
}

private[transducer] final class MapTransducer[B, A](f: A ⇒ B) extends Transducer[B, A] {
  def apply[R](rf: Reducer[B, R]) =
    new MapReducer[B, A, R](rf, f)
}

private[transducer] final class CollectTransducer[A, B](pf: PartialFunction[A, B]) extends Transducer[B, A] {
  def apply[R](rf: Reducer[B, R]) =
    new CollectReducer[A, B, R](rf, pf)
}

private[transducer] final class FlatMapTransducer[A, B, F[_]: AsSource](f: A ⇒ F[B]) extends Transducer[B, A] {
  def apply[R](rf: Reducer[B, R]) =
    new FlatMapReducer[A, B, R, F](rf, f)
}

private[transducer] final class TakeTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new TakeReducer[A, R](rf, n)
}

private[transducer] final class TakeWhileTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new TakeWhileReducer[A, R](rf, f)
}

private[transducer] final class TakeNthTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new TakeNthReducer[A, R](rf, n)
}

private[transducer] final class DropTransducer[A](n: Long) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DropReducer[A, R](rf, n)
}

private[transducer] final class DropWhileTransducer[A](f: A ⇒ Boolean) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DropWhileReducer[A, R](rf, f)
}

private[transducer] final class DropRightTransducer[A: ClassTag](n: Int) extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DropRightReducer[A, R](rf, n)
}

private[transducer] final class DistinctTransducer[A]() extends Transducer[A, A] {
  def apply[R](rf: Reducer[A, R]) =
    new DistinctReducer[A, R](rf)
}

private[transducer] final class BufferTransducer[A, F[_]](n: Int)(implicit F: AsTarget[F], S: Sized[F]) extends Transducer[F[A], A] {
  def apply[R](rf: Reducer[F[A], R]) =
    new BufferReducer[A, R, F](rf, n)
}

private[transducer] final class PartitionTransducer[A, B <: AnyRef, F[_]](f: A ⇒ B)(implicit F: AsTarget[F], S: Sized[F]) extends Transducer[F[A], A] {
  def apply[R](rf: Reducer[F[A], R]) =
    new PartitionReducer[A, B, R, F](rf, f)
}
