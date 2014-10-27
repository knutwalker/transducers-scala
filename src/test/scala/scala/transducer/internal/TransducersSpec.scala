package scala.transducer.internal

import org.scalatest.FunSuite

import scala.transducer.Transducer

class TransducersSpec extends FunSuite {

  def nil[A] = List.empty[A]
  def transduce[A, B](x: A*)(implicit tf: Transducer[A, B]): List[B] =
    transducer.transduceInit(tf)(nil[B], x.toList)

  test("the filter transducer") {
    implicit val tx = transducer.filter((_: String) == "x")
    assert(transduce[String, String]("x") == List("x"))
    assert(transduce[String, String]("f") == List())
  }

  test("the filterNot transducer") {
    implicit val tx = transducer.filterNot((_: String) == "x")
    assert(transduce[String, String]("x") == List())
    assert(transduce[String, String]("f") == List("f"))
  }

  test("the map transducer") {
    implicit val tx = transducer.map((_: Int).toString)
    assert(transduce[Int, String](42) == List("42"))
    assert(transduce[Int, String](1337) == List("1337"))
  }

  test("the collect transducer") {
    implicit val tx = transducer.collect[Int, String] {
      case 42   ⇒ "42"
      case 1337 ⇒ "1337"
    }
    assert(transduce[Int, String](42) == List("42"))
    assert(transduce[Int, String](1337) == List("1337"))
    assert(transduce[Int, String](13) == List())
  }

  test("the foreach transducer") {
    var effected = List.empty[String]
    implicit val tx = transducer.foreach((x: String) ⇒ effected = x :: effected)
    assert(transduce[String, Unit]("42", "1337") == List())
    assert(effected == List("1337", "42"))
  }

  test("the flatMap transducer") {
    implicit val tx = transducer.flatMap((x: String) ⇒ List(x, s"${x}${x}"))
    assert(transduce[String, String]("42", "1337") == List("42", "4242", "1337", "13371337"))
  }

  test("the take transducer") {
    implicit val tx = transducer.take[String](2)
    assert(transduce[String, String]("42", "1337", "24", "7331") == List("42", "1337"))
  }

  test("the takeWhile transducer") {
    implicit val tx = transducer.takeWhile((_: String) == "42")
    assert(transduce[String, String]("42", "42", "1337") == List("42", "42"))
  }

  test("the takeNth transducer") {
    implicit val tx = transducer.takeNth[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("142", "342"))
  }

  test("the takeRight transducer") {
    implicit val tx = transducer.takeRight[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("342", "442"))
  }

  test("the drop transducer") {
    implicit val tx = transducer.drop[String](2)
    assert(transduce[String, String]("24", "7331", "42", "1337") == List("42", "1337"))
  }

  test("the dropWhile transducer") {
    implicit val tx = transducer.dropWhile((_: String) == "42")
    assert(transduce[String, String]("42", "42", "1337") == List("1337"))
  }

  test("the dropNth transducer") {
    implicit val tx = transducer.dropNth[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("242", "442"))
  }

  test("the dropRight transducer") {
    implicit val tx = transducer.dropRight[String](2)
    assert(transduce[String, String]("142", "242", "342", "442") == List("142", "242"))
  }

  test("the distinct transducer") {
    implicit val tx = transducer.distinct[String]
    assert(transduce[String, String]("42", "42", "1337", "1337") == List("42", "1337"))
    assert(transduce[String, String]("42", "42", "1337", "42", "1337") == List("42", "1337", "42", "1337"))
  }

  test("the grouped transducer") {
    implicit val tx = transducer.grouped[String, List](2)
    assert(transduce[String, List[String]]("42", "42", "42", "1337", "1337", "1337") ==
      List(List("42", "42"), List("42", "1337"), List("1337", "1337")))
  }

  test("the partition transducer") {
    implicit val tx = transducer.partition[String, String, List]((_: String).length.toString)
    assert(transduce[String, List[String]]("42", "13", "37", "1337", "4242") ==
      List(List("42", "13", "37"), List("1337", "4242")))
  }
}

