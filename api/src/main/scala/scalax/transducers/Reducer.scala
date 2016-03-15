/*
 * Copyright 2014 – 2016 Paul Horn
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

import scalax.transducers.internal.Reduced

trait Reducer[@specialized(Int, Long, Double, Char, Boolean) A, R] extends ((R, A, Reduced) ⇒ R) with (R ⇒ R) {
  def apply(r: R, a: A, s: Reduced): R

  def apply(r: R): R

  def prepare(r: R, s: Reduced): R
}
