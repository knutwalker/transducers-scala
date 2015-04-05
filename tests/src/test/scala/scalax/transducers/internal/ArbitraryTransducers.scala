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

import transducers._

import scalaz.NonEmptyList

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary


trait ArbitraryTransducers extends Arbitraries {

  val genEmpty: Gen[Transducer[Int, Int]] =
    Gen.const(empty[Int, Int])

  val genNoop: Gen[Transducer[Int, Int]] =
    Gen.const(noop[Int])

  val genOrElse: Gen[Transducer[Int, Int]] =
    arbitrary[Int].map(n ⇒ orElse(n))

  val genForeach: Gen[Transducer[Int, Unit]] =
    Gen.const(foreach[Int](_ ⇒ ()))

  val genMap: Gen[Transducer[Int, Int]] =
    Function1IntInt.arbitrary.map(map)

  val genFlatMap: Gen[Transducer[Int, Int]] =
    Function1IntListInt.arbitrary.map(f ⇒ flatMap(f))

  val genFilter: Gen[Transducer[Int, Int]] =
    Function1IntBoolean.arbitrary.map(filter)

  val genFilterNot: Gen[Transducer[Int, Int]] =
    Function1IntBoolean.arbitrary.map(filterNot)

  val genCollect: Gen[Transducer[Int, Int]] =
    PartialFunctionIntInt.arbitrary.map(collect)

  val genCollectFirst: Gen[Transducer[Int, Int]] =
    PartialFunctionIntInt.arbitrary.map(collectFirst)

  val genFind: Gen[Transducer[Int, Int]] =
    Function1IntBoolean.arbitrary.map(find)

  val genForall: Gen[Transducer[Int, Boolean]] =
    Function1IntBoolean.arbitrary.map(forall)

  val genExists: Gen[Transducer[Int, Boolean]] =
    Function1IntBoolean.arbitrary.map(exists)

  val genFold: Gen[Transducer[Int, Int]] =
    Function2IntIntInt.arbitrary.flatMap(f ⇒ arbitrary[Int].map(z ⇒ fold(z)(f)))

  val genScan: Gen[Transducer[Int, Int]] =
    Function2IntIntInt.arbitrary.flatMap(f ⇒ arbitrary[Int].map(z ⇒ scan(z)(f)))

  val genHead: Gen[Transducer[Int, Int]] =
    Gen.const(head[Int])

  val genLast: Gen[Transducer[Int, Int]] =
    Gen.const(last[Int])

  val genInit: Gen[Transducer[Int, Int]] =
    Gen.const(init[Int])

  val genTail: Gen[Transducer[Int, Int]] =
    Gen.const(tail[Int])

  val genTake: Gen[Transducer[Int, Int]] =
    posNumGen.map(_.toLong).map(take)

  val genTakeWhile: Gen[Transducer[Int, Int]] =
    Function1IntBoolean.arbitrary.map(takeWhile)

  val genTakeRight: Gen[Transducer[Int, Int]] =
    posNumGen.map(_.toInt).map(takeRight)

  val genTakeNth: Gen[Transducer[Int, Int]] =
    posNumGen.map(_.toLong).map(takeNth)

  val genDrop: Gen[Transducer[Int, Int]] =
    posNumGen.map(_.toLong).map(drop)

  val genDropWhile: Gen[Transducer[Int, Int]] =
    Function1IntBoolean.arbitrary.map(dropWhile)

  val genDropRight: Gen[Transducer[Int, Int]] =
    posNumGen.map(_.toInt).map(dropRight)

  val genDropNth: Gen[Transducer[Int, Int]] =
    posNumGen.map(_.toLong).map(dropNth)

  val genSlice: Gen[Transducer[Int, Int]] =
    slicePairGen.map { case (n, m) ⇒ slice(m.toLong, n.toLong) }

  val genDistinct: Gen[Transducer[Int, Int]] =
    Gen.const(distinct[Int])

  val genZipWithIndex: Gen[Transducer[Int, (Int, Int)]] =
    Gen.const(zipWithIndex[Int])

  val genGrouped: Gen[Transducer[Int, List[Int]]] =
    posNumGen.map(_.toInt).map(n ⇒ grouped[Int, List](n))

  val genGroupBy: Gen[Transducer[Int, List[Int]]] =
    Gen.const(groupBy[Int, String, List](x ⇒ (x / 10).toString))

  val genTransducerIntInt: Gen[Transducer[Int, Int]] = Gen.oneOf(
    genEmpty,
    genNoop,
    genOrElse,
    genMap,
    genFlatMap,
    genFilter,
    genFilterNot,
    genCollect,
    genCollectFirst,
    genFind,
    genFold,
    genScan,
    genHead,
    genLast,
    genInit,
    genTail,
    genTake,
    genTakeWhile,
    genTakeRight,
    genTakeNth,
    genDrop,
    genDropWhile,
    genDropRight,
    genDropNth,
    genSlice,
    genDistinct
  )

  val intTransducer: Arbitrary[Transducer[Int, Int]] =
    Arbitrary(genTransducerIntInt)

  def intTransducers(ts: Arbitrary[Transducer[Int, Int]]): Arbitrary[Transducer[Int, Int]] =
    Arbitrary(arbNel(ts).map { txs ⇒
      NonEmptyList.nonEmptyList.foldl1(txs)(t ⇒ x ⇒ t >> x)
    })

  def halfSized[A: Arbitrary]: Gen[A] =
    Gen.sized(s ⇒ Gen.choose(0, s / 2).flatMap(t ⇒ Gen.resize(t, arbitrary[A])))

  def arbNel[A: Arbitrary]: Gen[NonEmptyList[A]] =
    arbitrary[A].flatMap(x ⇒ halfSized[List[A]].map(xs ⇒ NonEmptyList.nel(x, xs)))
}
object ArbitraryTransducers extends ArbitraryTransducers
