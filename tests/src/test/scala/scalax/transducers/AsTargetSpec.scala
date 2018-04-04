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

package scalax.transducers

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object AsTargetSpec extends Specification with ScalaCheck {

  "iterator as target" should {
    val at = AsTarget.iterator
    "not consume anything when calling size" in prop { (xs: List[Int]) ⇒
      var builder = at.empty[Int]
      xs foreach (x ⇒ builder = at.append(builder, x))

      at.size(builder) ==== xs.size
      at.finish(builder).toList ==== xs
    }
  }
}
