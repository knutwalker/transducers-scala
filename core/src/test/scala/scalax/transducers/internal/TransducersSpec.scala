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
package scalax
package transducers.internal

import org.scalatest.FunSuite

import scalax.transducers.Transducer

class TransducersSpec extends FunSuite {

  def nil[A] = List.empty[A]
  def transduce[A, B](x: A*)(implicit tf: Transducer[A, B]): List[B] =
    transducers.transduceInit(tf)(nil[B], x.toList)

  test("the empty transducer") {
    implicit val tx = transducers.empty[Int]
    assert(transduce[Int, Int](1, 2, 13, 42, 1337) == List.empty[Int])
  }

  test("the filter transducer") {
    implicit val tx = transducers.filter((_: String) == "x")
    assert(transduce[String, String]("x") == List("x"))
    assert(transduce[String, String]("f") == List())
  }

  test("the filterNot transducer") {
    implicit val tx = transducers.filterNot((_: String) == "x")
    assert(transduce[String, String]("x") == List())
    assert(transduce[String, String]("f") == List("f"))
  }

  test("the map transducer") {
    implicit val tx = transducers.map((_: Int).toString)
    assert(transduce[Int, String](42) == List("42"))
    assert(transduce[Int, String](1337) == List("1337"))
  }

  test("the collect transducer") {
    implicit val tx = transducers.collect[Int, String] {
      case 42   ⇒ "42"
      case 1337 ⇒ "1337"
    }
    assert(transduce[Int, String](42) == List("42"))
    assert(transduce[Int, String](1337) == List("1337"))
    assert(transduce[Int, String](13) == List())
  }

  test("the foreach transducer") {
    var effected = List.empty[String]
    implicit val tx = transducers.foreach((x: String) ⇒ effected = x :: effected)
    assert(transduce[String, Unit]("42", "1337") == List())
    assert(effected == List("1337", "42"))
  }

  test("the flatMap transducer") {
    implicit val tx = transducers.flatMap((x: String) ⇒ List(x, s"${x}${x}"))
    assert(transduce[String, String]("42", "1337") == List("42", "4242", "1337", "13371337"))
  }

  test("the take transducer") {
    implicit val tx = transducers.take[String](2)
    assert(transduce[String, String]("42", "1337", "24", "7331") == List("42", "1337"))
  }

  test("the takeWhile transducer") {
    implicit val tx = transducers.takeWhile((_: String) == "42")
    assert(transduce[String, String]("42", "42", "1337") == List("42", "42"))
  }

  test("the takeNth transducer") {
    implicit val tx = transducers.takeNth[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("142", "342"))
  }

  test("the takeRight transducer") {
    implicit val tx = transducers.takeRight[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("342", "442"))
  }

  test("the drop transducer") {
    implicit val tx = transducers.drop[String](2)
    assert(transduce[String, String]("24", "7331", "42", "1337") == List("42", "1337"))
  }

  test("the dropWhile transducer") {
    implicit val tx = transducers.dropWhile((_: String) == "42")
    assert(transduce[String, String]("42", "42", "1337") == List("1337"))
  }

  test("the dropNth transducer") {
    implicit val tx = transducers.dropNth[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("242", "442"))
  }

  test("the dropRight transducer") {
    implicit val tx = transducers.dropRight[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("142", "242"))
  }

  test("the distinct transducer") {
    implicit val tx = transducers.distinct[String]
    assert(transduce[String, String]("42", "42", "1337", "1337") == List("42", "1337"))
    assert(transduce[String, String]("42", "42", "1337", "42", "1337") == List("42", "1337", "42", "1337"))
  }

  test("the zipWithIndex transducer") {
    implicit val tx = transducers.zipWithIndex[String]
    assert(transduce[String, (String, Int)]("42", "1337", "421337") ==
      List(("42", 0), ("1337", 1), ("421337", 2)))
  }

  test("the grouped transducer") {
    implicit val tx = transducers.grouped[String, List](2)
    assert(transduce[String, List[String]]("42", "42", "42", "1337", "1337", "1337") ==
      List(List("42", "42"), List("42", "1337"), List("1337", "1337")))
  }

  test("the groupBy transducer") {
    implicit val tx = transducers.groupBy[String, String, List]((_: String).length.toString)
    assert(transduce[String, List[String]]("42", "13", "37", "1337", "4242") ==
      List(List("42", "13", "37"), List("1337", "4242")))
  }
}

