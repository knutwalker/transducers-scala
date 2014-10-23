package example

import scala.transducer._


object DRMain extends App {

  val xform = map((_: Int).toString + "x")
    .dropRight(5)
    .take(45)
    .flatMap(_.toList)
    .dropRight(2)
    .buffer[List](5)
    .partition[Vector](_.count(_ == 'x').toString)

//  val data = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  val data = Iterator.from(1)

  val result = into[List].run(xform)(data)
  println(s"result = $result")
}

object Main extends App {

  val data = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  {
    val xform = filter((_: Int) % 2 == 0)
      .map(_.toString + "1")
      .flatMap(_.toList)
      .take(9)
      .drop(2)

    // run uses the same shape as input
    val result = run(xform)(data)
    println(s"run from list = $result")
  }

  {
    val isEven = filter((_: Int) % 2 == 0)
    val plusOne = map((_: Int).toString + "1")
    val splitChars = flatMap((_: String).toList)
    val first3 = take[Char](3)
    val lastOne = drop[Char](2)

    // alternate syntax
    val xform = isEven >> plusOne >> splitChars >> first3 >> lastOne
    // into can change the shape
    val result = into[Stream].run(xform)(data)
    println(s"into Stream = $result")
    println(s"result.toList = ${result.toList}")
  }

  {
    val xform = filter((_: Int) % 2 == 0)
      .map(_.toString + "x")
      .take(9)
      .drop(2)

    val ints = Iterator.from(0)

    // with drop/take in place, you can use infinite collections
    val result = into[List].run(xform)(ints)
    println(s"from infinite collection = $result")

    // into[F[_]].from[G[_]].run(t: Transducer[A, B])  ==>
    // G[B] => F[A]
    val t: (Iterator[Int]) => List[String] = into[List].from[Iterator].run(xform) _

    println(s"t(Iterator.from(0)) = ${t(Iterator.from(0))}")
    println(s"t2(Iterator(10, 8, 6, 5, 3, 1)) = ${t(Iterator(10, 8, 6, 5, 3, 1))}")

  }


  {
    val xform = filter((_: Int) % 2 == 0)
      .map(_.toString + "x")
      .take(4)
      .drop(2)

    val ints = Iterator.from(0)

    // with drop/take in place, you can use infinite collections
    val result = into(List("a", "b", "c"))(xform)(ints)
    println(s"using custom initial value = $result")
  }

}
