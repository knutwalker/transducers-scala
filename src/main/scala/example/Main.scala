package example

import scala.transducer._

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
    val xform: Transducer[String, Int] = filter((_: Int) % 2 == 0)
      .map(_.toString + "x")
      .take(9)
      .drop(2)

    val ints = Iterator.from(0)

    // with drop/take in place, you can use infinite collections
    val result = into[List].run(xform)(ints)
    println(s"from infinite collection = $result")

    val x = into[List]
    val y = x.from[Iterator]
    val t = y.run(xform) _

    // into[F[_]].from[G[_]].run(t: Transducer[A, B])  ==>
    // G[B] => F[A]
    val t2: (Iterator[Int]) => List[String] = into[List].from[Iterator].run(xform) _

    println(s"t(Iterator.from(0)) = ${t(Iterator.from(0))}")
    println(s"t2(Iterator(10, 8, 6, 5, 3, 1)) = ${t2(Iterator(10, 8, 6, 5, 3, 1))}")

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
