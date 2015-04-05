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

package scalax.transducers

import scalaz.{@@, Tag}

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Arbitrary, Gen}

import scala.math.max

trait Arbitraries {

  type =?>[A, B] = PartialFunction[A, B]

  // anonymous instances so that parameter reporting in error cases
  // don't just show '<function1>' but betray the actual function

  final val intIdentity: Int ⇒ Int = new (Int ⇒ Int) {
    def apply(v1: Int): Int = v1
    override def toString(): String = "intIdentity"
  }
  final val intPlusOne: Int ⇒ Int = new (Int ⇒ Int) {
    def apply(v1: Int): Int = v1 + 1
    override def toString(): String = "intPlusOne"
  }
  final def intConstant(a: Int): Int ⇒ Int = new (Int ⇒ Int) {
    def apply(v1: Int): Int = a
    override def toString(): String = s"intConstant($a)"
  }
  final val alwaysFalse: Int ⇒ Boolean = new (Int ⇒ Boolean) {
    def apply(v1: Int): Boolean = false
    override def toString(): String = "alwaysFalse"
  }
  final val alwaysTrue: Int ⇒ Boolean = new (Int ⇒ Boolean) {
    def apply(v1: Int): Boolean = true
    override def toString(): String = "alwaysTrue"
  }
  final val evens: Int ⇒ Boolean = new (Int ⇒ Boolean) {
    def apply(v1: Int): Boolean = v1 % 2 == 0
    override def toString(): String = "evens"
  }
  final val unevens: Int ⇒ Boolean = new (Int ⇒ Boolean) {
    def apply(v1: Int): Boolean = v1 % 2 != 0
    override def toString(): String = "unevens"
  }
  final val emptyList: Int ⇒ List[Int] = new (Int ⇒ List[Int]) {
    def apply(v1: Int): List[Int] = List()
    override def toString(): String = "emptyList"
  }
  final val singletonList: Int ⇒ List[Int] = new (Int ⇒ List[Int]) {
    def apply(v1: Int): List[Int] = List(v1)
    override def toString(): String = "singletonList"
  }
  final val doublesList: Int ⇒ List[Int] = new (Int ⇒ List[Int]) {
    def apply(v1: Int): List[Int] = List(v1, v1)
    override def toString(): String = "doublesList"
  }
  final def sizedList(a: Int): Int ⇒ List[Int] = new (Int ⇒ List[Int]) {
    def apply(v1: Int): List[Int] = List.fill(a)(v1)
    override def toString(): String = s"sizedList($a)"
  }
  final val addition: (Int, Int) ⇒ Int = new ((Int, Int) ⇒ Int) {
    def apply(v1: Int, v2: Int): Int = v1 + v2
    override def toString(): String = "addition"
  }
  final val subtraction: (Int, Int) ⇒ Int = new ((Int, Int) ⇒ Int) {
    def apply(v1: Int, v2: Int): Int = v1 - v2
    override def toString(): String = "subtraction"
  }
  final val multiplication: (Int, Int) ⇒ Int = new ((Int, Int) ⇒ Int) {
    def apply(v1: Int, v2: Int): Int = v1 * v2
    override def toString(): String = "multiplication"
  }
  final val division: (Int, Int) ⇒ Int = new ((Int, Int) ⇒ Int) {
    def apply(v1: Int, v2: Int): Int = if (v2 == 0) v1 else v1 / v2
    override def toString(): String = "division"
  }

  implicit val Function1IntInt: Arbitrary[Int => Int] =
    Arbitrary(Gen.frequency[Int => Int](
      1 → Gen.const(intIdentity),
      1 → Gen.const(intPlusOne),
      3 → arbitrary[Int].map(intConstant)
    ))

  implicit val Function1IntBoolean: Arbitrary[Int => Boolean] =
    Arbitrary(Gen.frequency[Int => Boolean](
      1 → Gen.const(alwaysFalse),
      1 → Gen.const(alwaysTrue),
      1 → Gen.const(evens),
      1 → Gen.const(unevens)
    ))

  implicit val Function1IntListInt: Arbitrary[Int => List[Int]] =
    Arbitrary(Gen.frequency[Int => List[Int]](
      2 → Gen.const(emptyList),
      2 → Gen.const(singletonList),
      2 → Gen.const(doublesList),
      1 → Gen.sized(a ⇒ Gen.const(sizedList(a)))
    ))

  implicit val Function2IntIntInt: Arbitrary[(Int, Int) => Int] =
    Arbitrary(Gen.frequency[(Int, Int) => Int](
      1 → Gen.const(addition),
      1 → Gen.const(subtraction),
      1 → Gen.const(multiplication),
      1 → Gen.const(division)
    ))

  implicit val PartialFunctionIntInt: Arbitrary[Int =?> Int] =
    Arbitrary(for {
      f ← arbitrary[Int ⇒ Boolean]
      g ← arbitrary[Int ⇒ Int]
    } yield {
      val pf: Int =?> Int = new (Int =?> Int) {
        def isDefinedAt(x: Int): Boolean = f(x)
        def apply(v1: Int): Int = g(v1)
        override def toString(): String = s"partial($f =?> $g)"
      }
      pf
    })

  val posNumGen: Gen[Int @@ Positive] =
    Gen.sized(m ⇒ Choose.chooseInt.choose(0, m * 2).map(Tag(_)))
  implicit val posNum = Arbitrary(posNumGen)

  private val negNumGen: Gen[Int @@ Negative] =
    Gen.sized(m ⇒ Choose.chooseInt.choose(-m * 2, 0).map(Tag(_)))
  implicit val negNum = Arbitrary(negNumGen)

  val posNonZeroNumGen: Gen[Int @@ NonZeroPositive] =
    Gen.sized(m ⇒ Choose.chooseInt.choose(1, max(1, m * 2)).map(Tag(_)))
  implicit val posNonZeroNum = Arbitrary(posNonZeroNumGen)

  private val atLeastTwoGen: Gen[Int @@ AtLeastTwo] =
    Gen.sized(m ⇒ Choose.chooseInt.choose(2, max(2, m * 2)).map(Tag(_)))
  implicit val atLeastTwo = Arbitrary(atLeastTwoGen)

  private val oneOrLessGen: Gen[Int @@ OneOrLess] =
    Gen.sized(m ⇒ Choose.chooseInt.choose(-m * 2, 1).map(Tag(_)))
  implicit val oneOrLess = Arbitrary(oneOrLessGen)

  val slicePairGen: Gen[(Int, Int)] = for (x ← Tag.unsubst(posNumGen); y ← Tag.unsubst(posNumGen)) yield {
    if (x > y) (y, x) else (x, y)
  }
  implicit val slicePair = Arbitrary(slicePairGen)

  implicit def unwrapPositive(pi: Int @@ Positive): Int = Tag.unwrap(pi)
  implicit def unwrapNonZeroPositive(pi: Int @@ NonZeroPositive): Int = Tag.unwrap(pi)
  implicit def unwrapAtLeastTwo(pi: Int @@ AtLeastTwo): Int = Tag.unwrap(pi)
  implicit def unwrapOneOrLess(pi: Int @@ OneOrLess): Int = Tag.unwrap(pi)
  implicit def unwrapNegative(pi: Int @@ Negative): Int = Tag.unwrap(pi)

  sealed trait Negative
  sealed trait Positive
  sealed trait NonZeroPositive
  sealed trait AtLeastTwo
  sealed trait OneOrLess
}
object Arbitraries extends Arbitraries
