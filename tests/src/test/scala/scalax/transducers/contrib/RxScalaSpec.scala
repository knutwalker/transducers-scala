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

package scalax.transducers.contrib

import scalax.transducers.ContribTransducer

import org.specs2.mutable.Specification
import rx.lang.scala.Observable
import rx.lang.scala.JavaConversions._

final class RxScalaSpec extends Specification with RxSupport with ContribTransducer {

  "The RxScala support" should {
    tag("contrib")
    "transduce on an RxObservable" in {
      val ints: Observable[Integer] = rx.Observable.range(0, Int.MaxValue)
      val observable = ints.map(Integer2int).transduce(testTx)
      val result = observable.toBlocking.toList

      result ==== expectedResult
    }
  }

}
