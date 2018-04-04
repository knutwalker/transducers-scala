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

package scalax.transducers.internal

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

object ReducedSpec extends Specification with ScalaCheck {

  "Reduced" should {

    "start with false" in {
      val r = new Reduced
      r.? ==== false
    }

    "switch to true, when applied" in prop { (x: Int) ⇒
      val r = new Reduced
      r(x) ==== x
      r.? ==== true
    }

    "fail when applied multiple times" in prop { (x: Int) ⇒
      val r = new Reduced
      r(x)

      r(x) must throwA[IllegalStateException].like {
        case e ⇒ e.getMessage must startWith("ContractViolation")
      }
    }

    "toString" in {
      val r1, r2 = new Reduced
      r1(0)
      r1.toString ==== "Reduced(true)"
      r2.toString ==== "Reduced(false)"
    }


  }

}
