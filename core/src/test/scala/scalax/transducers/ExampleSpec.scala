/*
 * Copyright 2014 Paul Horn
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
package scalax.transducers

import org.scalatest.FunSpec

class ExampleSpec extends FunSpec {
  import scalax.transducers

  describe("The transducer") {

    it("can be build independently from its source or target ") {

      // create a simple transducer, that will filter out odd numbers
      val f = transducers.filter[Int](_ % 2 == 0)

      // create some input data
      val data = (1 to 10).toList

      // `run` the transducer.
      // Just using `run` will use the same shape of the input data
      // for the result.
      val result = transducers.run(f)(data)

      assert(result == List(2, 4, 6, 8, 10))
    }

    it("can be composed from smaller reducers using compose") {

      // create a simple transducer, that will filter out odd numbers
      val f = transducers.filter[Int](_ % 2 == 0)

      // use `compose` to create new Transducers
      val g = transducers.map((_: String).toInt)
      // h is the same as using x => f(g(x)) if these were simple functions
      val h = f compose g

      val result = transducers.run(h)(List("1", "4", "9", "12"))
      assert(result == List(4, 12))
    }

    it("can be composed using andThen as well") {

      // create a simple transducer, that will filter out odd numbers
      val f = transducers.filter[Int](_ % 2 == 0)

      // use `andThen` to create new Transducers
      val g = transducers.map((_: Int).toString)
      // h is the same as using x => g(f(x)) if these were simple functions
      val h = f andThen g
      // `>>` is an alternative syntax for `andThen`
      val h2 = f >> g

      val result = transducers.run(h)((1 to 10).toList)
      assert(result == List("2", "4", "6", "8", "10"))
    }

    it("can be chained like ordinary collections") {

      // `t` behaves as expected, the data flows from left to right.
      // This form supports better type inference, since the input
      // type is already defined by the previous transducer.
      val t = transducers.filter[Int](_ % 2 == 0)
        .map(_ + 16)
        .map(_.toString)
        .flatMap(_.toList)

      val result = transducers.run(t)((1 to 5).toList)
      assert(result == List('1', '8', '2', '0'))
    }

    it("supports early termination") {

      // The early termination of e.g. `take` means,
      // that the process completes after three elements.
      val t = transducers.map((x: Int) ⇒ {
        println(s"side-effect in first map: x = $x")
        x + 1
      }).map(_ + 1)
        .map(_ + 1)
        .take(3)

      val result = transducers.run(t)((1 to 100).toList)
      assert(result == List(4, 5, 6))
    }

    it("supports laziness") {

      // Together with early termination, laziness can achieve,
      // that transducers can be run on infinite collections and
      // complete within a finite time-frame.
      val t = transducers.filter[Int](x ⇒ {
        println(s"side-effect in filter: x = $x")
        x % 2 == 0
      }).dropRight(5)
        .take(5)

      // Normally, a `dropRight` on an infinite stream would not complete
      // since `dropRight` had to eagerly evaluate the complete stream
      // to determine its end. With `take`, only the first 20 elements
      // will be traversed:
      //  10 will pass the `filter`
      //  the first 5 will be `take`n and the second 5 will be `drop`ped
      val result = transducers.run(t)(Stream.from(1))
      assert(result.toList == List(2, 4, 6, 8, 10))
    }

    it("can change the shape if the output") {
      val t = transducers.map((_: Int) + 1)

      // `into` can be used to change the shape of the result collection.
      // The shape has to be a first-order kinded type
      //  e.g. `F[_]` or `* -> *`
      // It uses a type class to know how to create an empty instance
      // for a type and how to append values
      val result = transducers.into[Vector].run(t)((1 to 10).iterator)
      assert(result == Vector(2, 3, 4, 5, 6, 7, 8, 9, 10, 11))
    }

    it("can append to a different shaped output") {
      val t = transducers.map((_: Int) + 1)

      // `addto` can be used to add data to an existing collection
      // which does not have to have the same shape.
      // The same rules from `into` apply as well, only that there's
      // no need to create an empty collection first
      val result = transducers.addto(Vector(0, 1))(t)((1 to 10).iterator)
      assert(result == Vector(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))
    }

    it("can be applied with the input shape to create a function") {
      val t = transducers.map((_: Int) + 1)

      // `into[F].from[G]`, where `G` is also a first-order kinded type
      // (but with a different type class) can be used to
      // specify the abstracted in/out types and get a function from
      // `F[A] => G[B]` for any `Transducer[A, B]`
      // The type ascription is only for refernce and will be inferred
      // if ommited.
      val f: (Iterator[Int]) ⇒ Vector[Int] =
        into[Vector].from[Iterator].run(t)

      val data = (1 to 10).iterator
      val result = f(data)
      assert(result == Vector(2, 3, 4, 5, 6, 7, 8, 9, 10, 11))
    }
  }
}
