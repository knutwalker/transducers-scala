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

package scalax
package transducers.internal

import scalaz.{NonEmptyList, Tag, @@}

import org.scalacheck.{Gen, Arbitrary}
import org.scalacheck.Arbitrary.arbitrary

import transducers.Transducer

import org.specs2._
import org.specs2.mutable.Specification


object CombiningSpec extends Specification with ScalaCheck with ArbitraryTransducers with TransducerTests {

  "empty" should {
    implicit val transducersWithoutEmpty: Arbitrary[Transducer[Int, Int]] =
      intTransducers(genNonEmpty)

    "immediately finish execution if it is at the end" in prop { (xs: List[Int], tx0: Transducer[Int, Int]) ⇒
      val tx = tx0.empty[Int]
      run(xs, tx).length ==== 0
      consume(xs, tx) ==== 0
    }

    "can appear two times in a row" in prop { (xs: List[Int], tx0: Transducer[Int, Int]) ⇒
      val tx = tx0.empty[Int].empty[Int]
      run(xs, tx).length ==== 0
      consume(xs, tx) ==== 0
    }
  }

  val genNonEmpty: Arbitrary[Transducer[Int, Int]] =
    Arbitrary(genTransducerIntInt.suchThat(!_.isInstanceOf[EmptyTransducer[Int, Int]]))
}
