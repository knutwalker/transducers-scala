package example

import scala.transducer._

object Main extends App {

  val data = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  {
    val xform = filter((_: Int) % 2 == 0)
      .map(_.toString + "1")
      .flatMap(_.toList)
      .take(3)
      .drop(2)

    val result = run(data, xform) // same shape as input
    println(s"result = ${result}")
  }

  {
    val isEven = filter((_: Int) % 2 == 0)
    val plusOne = map((_: Int).toString + "1")
    val splitChars = flatMap((_: String).toList)
    val first6 = take[Char](6)

    val xform = isEven >> plusOne >> splitChars >> first6 // alternate syntax
  val result = into[Stream].run(data, xform) // shape may change
    println(s"result = ${result}")
    println(s"result.toList = ${result.toList}")
  }
}
