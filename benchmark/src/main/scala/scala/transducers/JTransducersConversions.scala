package scala.transducers

import java.util.concurrent.atomic.AtomicBoolean
import java.util.function

import com.cognitect.transducers.IStepFunction

import scala.language.implicitConversions

object JTransducersConversions {
  implicit def fn2cognFn[A, B](f: A => B): com.cognitect.transducers.Function[A, B] =
    new com.cognitect.transducers.Function[A, B] {
      def apply(t: A) = f(t)
    }

  implicit def fn2cognStep[A, B](f: (A, B) => A): IStepFunction[A, B] =
    new IStepFunction[A, B] {
      def apply(result: A, input: B, reduced: AtomicBoolean) = f(result, input)
    }

  implicit def fn2jfn[A, B](f: A => B): function.Function[A, B] =
    new function.Function[A, B] {
      def apply(t: A) = f(t)
    }
}
