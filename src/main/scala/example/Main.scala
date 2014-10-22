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

    // run uses the same shape as input
    val result = run(data, xform)
    println(s"result = ${result}")
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
    val result = into[Stream].run(data, xform)
    println(s"result = ${result}")
    println(s"result.toList = ${result.toList}")
  }
}
