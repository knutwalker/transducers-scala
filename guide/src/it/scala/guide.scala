/*
 * Copyright 2014 – 2015 Paul Horn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import scalax.transducers

import scalax.transducers.AsTarget
import scalax.transducers.internal.TransducersSpec.CountingIterator

import org.specs2.Specification
import org.specs2.execute.SnippetParams
import org.specs2.specification.Snippets

// scalastyle:off
object guide extends Specification with Snippets { lazy val is = "Transducer Usage Guide".title ^ s2"""
Transducers are a way to build reusable transformations.

Let's start with some input data `xs`, that will be used for further examples:
${snippet{
  (1 to 10).toList
}}

And we will use this simple transducer `tx`, that will filter out odd numbers:
${snippet{
  transducers.filter((x: Int) ⇒ x % 2 == 0)
}}

### Decoupling from in- and output


A transducer is independent from its source or target. Notice how the
definition of the transducer did not involve anything from the source
or the target.


If you `run` the transducer, it will use the input shape for the output.

$e1

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  transducers.run(tx)(xs)
}}


You can also change the output shape using `into`:

$e2

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  transducers.into[Vector].run(tx)(xs)
}}


The shape has to be a first-order kinded type, i.e. `F[_]` or `* -> *`.
There must be an instance of `AsTarget[F]` available. For some types,
there is already an instance available.
${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  (transducers.into[List].run(tx)(xs)
  , transducers.into[Vector].run(tx)(xs)
  , transducers.into[Stream].run(tx)(xs)
  , transducers.into[Set].run(tx)(xs)
  , transducers.into[Iterator].run(tx)(xs)
  , transducers.into[Iterable].run(tx)(xs)
  , transducers.into[Option].run(tx)(xs)
  , transducers.into[Option](AsTarget.lastOption).run(tx)(xs)
  )
}}


Note that choosing `Stream` won't automatically stop the consumption of the
input, when the stream is not consumed. Also, using `Option` will not
terminate the input consumption early. This might change in the future
(PR welcome ;-) ).


If you already have some target data available, you can use `addto`:

$e3

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  val result = (-10 to 0 by 2).toVector
  transducers.addto(result).run(tx)(xs)
}}


The same things about `AsTarget` apply for `addto` as they do for `into`.


All three methods also support a variant where the arguments are reversed
(`(input)(transducer)`) which works better with type inference, if the
transducer is declared inline:
${snippet{
  // 8<--
  val xs = (1 to 10).toList
  // 8<--
  transducers.run(transducers.filter((x: Int) ⇒ x % 2 == 0))(xs)
  //  transducers.run(transducers.filter(_ % 2 == 0))(xs)  // wouldn't compile
  transducers.run(xs)(transducers.filter(_ % 2 == 0))
}}


`into` and `addto` also have a `from` method, where you can define the input
shape, and a transducer.
You get a function from input to output:

$e4

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  val fn: List[Int] ⇒ Vector[Int] = transducers.into[Vector].from[List].run(tx)
  fn(xs)
}}


### Composing Transducers


Transducers can be composed as if they were functions (a `Transducer[A, B]`
is basically just a `Reducer[B, _] ⇒ Reducer[A, _]`).


You can use `compose` to create a new Transducer:

$e5

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  val tx2 = transducers.map((_: Int) * 5)
  val tx0 = tx compose tx2 // first map (*5), then filter (even?)

  transducers.run(tx0)(xs)
}}


You can also use `andThen`

$e6

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  val tx2 = transducers.map((_: Int) * 4)
  val tx0 = tx andThen tx2 // first filter (even?), then map (*4)
  transducers.run(tx0)(xs)
}}


`>>` is an alias for `andThen`

$e7

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  val tx2 = transducers.map((_: Int) * 4)
  val tx0 = tx >> tx2 // first filter (even?), then map (*4)
  transducers.run(tx0)(xs)
}}


Instead of creating the transducers beforehand and fighting the
type-inference, you directly chain transducers by calling the corresponding
methods.


This way, the API looks like that of a collection type

$e8

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  // 8<--
  val tx0 = tx map (4 *) map (2 +) drop 4
  transducers.run(tx0)(xs)
}}


### Laziness and early termination


Transducers are inherently lazy in their execution, similar to views. Unlike
in normal collections, every step is executed in every transducer before the
next value is computed.


To demonstrate this, the input is wrapped in an iterator `it`, that counts
how often it was consumed
${snippet{
  // 8<--
  val xs = (1 to 10).toList
  // 8<--
  CountingIterator(xs)
}}


So, take terminates early if everything is taken

$e9

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  val xs = (1 to 10).toList
  val it = CountingIterator(xs)
  // 8<--
  val tx0 = tx.take(2)
  (transducers.into[List].run(tx0)(it.it), it.consumed)
}}


Here, only 4 items were consumed (out of 10), because after 4 items, 2 were
found that matched the filter and the process could be terminated.


Another effect of lazy evaluation is, that transducers can operate on
infinite collections where the default scala collection operators would fail
to terminate.


Consider this example in Scala:
${snippet{
  Stream.from(1).dropRight(5).take(5)
  // java.lang.OutOfMemoryError: GC overhead limit exceeded
}}


This works with transducers

$e10

${snippet{
  // 8<--
  val tx = transducers.filter[Int](_ % 2 == 0)
  // 8<--
  val xs = Stream.from(1)
  val it = CountingIterator(xs)
  val tx0 = tx.dropRight(5).take(5)

  (transducers.into[List].run(tx0)(it.it), it.consumed)
}}


20 Items are consumed, 10 of which pass the filter condition; 5 are buffered
in case they need to be dropped and 5 will be taken until the process
terminates.

$p

"""

  lazy val e1 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList
    val result = transducers.run(tx)(xs)
    result ==== List(2, 4, 6, 8, 10)
  }

  lazy val e2 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList
    val result = transducers.into[Vector].run(tx)(xs)
    result ==== Vector(2, 4, 6, 8, 10)
  }

  lazy val e3 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList
    val init = (-10 to 0 by 2).toVector
    val result = transducers.addto(init).run(tx)(xs)
    result ==== Vector(-10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10)
  }

  lazy val e4 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList
    val fn = transducers.into[Vector].from[List].run(tx)
    val result = fn(xs)
    result ==== Vector(2, 4, 6, 8, 10)
  }

  lazy val e5 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList

    val tx2 = transducers.map((_: Int) * 5)
    val tx0 = tx compose tx2

    val result = transducers.run(tx0)(xs)
    result ==== List(10, 20, 30, 40, 50)
  }

  lazy val e6 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList

    val tx2 = transducers.map((_: Int) * 4)
    val tx0 = tx andThen tx2

    val result = transducers.run(tx0)(xs)
    result ==== List(8, 16, 24, 32, 40)
  }

  lazy val e7 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList

    val tx2 = transducers.map((_: Int) * 4)
    val tx0 = tx >> tx2

    val result = transducers.run(tx0)(xs)
    result ==== List(8, 16, 24, 32, 40)
  }

  lazy val e8 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList

    val tx0 = tx map (4*) map (2+) drop 4

    val result = transducers.run(tx0)(xs)
    result ==== List(42)
  }

  lazy val e9 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = (1 to 10).toList
    val it = CountingIterator(xs)
    val tx0 = tx.take(2)

    val result = transducers.into[List].run(tx0)(it.it)
    result ==== List(2, 4)
    it.consumed ==== 4
  }

  lazy val e10 = {
    val tx = transducers.filter[Int](_ % 2 == 0)
    val xs = Stream.from(1)
    val it = CountingIterator(xs)
    val tx0 = tx.dropRight(5).take(5)

    val result = transducers.into[List].run(tx0)(it.it)
    result ==== List(2, 4, 6, 8, 10)
    it.consumed ==== 20
  }

  override implicit def defaultSnippetParameters[T]: SnippetParams[T] =
    SnippetParams[T]().offsetIs(-4).eval

  implicit val dontEvalStream: SnippetParams[Stream[Int]] =
    SnippetParams[Stream[Int]]().offsetIs(-4)
}
