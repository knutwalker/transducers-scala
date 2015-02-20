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

package org.example

import org.scalatest.FunSuite
import rx.lang.scala.Observable

import scalax.transducers.ContribTransducer
import scalax.transducers.contrib.RxSupport

class RxSpec extends FunSuite with RxSupport with ContribTransducer {

  test("transducing on an rx observable") {

    val observable: Observable[(Char, Int)] = Observable.from(new Iterable[Int] {
      def iterator = Iterator.from(0)
    }).transduce(testTx)

    val result = observable.toBlocking.toList

    assert(result == expectedResult)
  }
}
