/*
 * Copyright 2014 – 2018 Paul Horn
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
package transducers.benchmark

import java.util
import fj.data.{List ⇒ FJList}

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
class Input {

  @Param(Array("100", "10000", "1000000"))
  private[this] final var _size: Int = 0

  private[this] final var _xs: List[Int] = Nil
  def xs: List[Int] = _xs

  private[this] final var _jxs: util.List[Int] = _
  def jxs: util.List[Int] = _jxs

  private[this] final var _fjxs: FJList[Int] = _
  def fjxs: FJList[Int] = _fjxs

  @Setup
  def setup(): Unit = {
    _xs = (1 to _size).toList
    _jxs = util.Arrays.asList[Int](_xs: _*)
    _fjxs = FJList.list(_xs: _*)
  }
}
