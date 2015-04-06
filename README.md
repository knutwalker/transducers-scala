![Travis CI](https://img.shields.io/travis/knutwalker/transducers-scala/master.svg)

# transducers-scala

Transducers are a way to build reusable transformations.

Put simply, transducers are like operations on collections (e.g. `map` and `filter`)
but without being bound to any collection or result type. Thus, they can be reused
in many contexts and carry only the simple processing logic.

## Getting it

`transducers-scala` is published to sonatype:

```scala
libraryDependencies += "de.knutwalker" %% "transducers-scala" % "0.3.0"
```

### Snapshot version

A snapshot is available at sonatype:

```scala
resolvers += "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += "de.knutwalker" %% "transducers-scala" % "0.4.0-SNAPSHOT"
```


Alternatively, clone this repo and run

```bash
./sbt +publishLocal # for ivy-style repositories
./sbt +publishM2    # for maven-style repositories
```


## Using it

Add `"de.knutwalker" %% "transducers-scala" % "0.3.0"` to your dependencies.
The full artifact names are `transducers-scala_2.10` and `transducers-scala_2.11`, so
this library is available for Scala 2.10 and 2.11.

After that, simply use `scalax.transducers._`

```scala
import scalax.transducers._

val xform = filter[Int](_ % 2 == 0).map(_.toString).flatMap(_.toList).take(10)
val data = Stream.from(13)
val result = into[List].run(xform)(data)
assert(result == List('1', '4', '1', '6', '1', '8', '2', '0', '2', '2'))
```

For more examples and a better introduction, see the [Usage Guide](guide/src/it/scala/guide.scala)


## Explanation

In short, transducers transform reducers.
Reducers combine some result and a value together to a new result.
Reducers are similar to the binary operations, one uses with folds.
A Transducer takes a reducer and return a transformed reducer.
The crux is, that neither reducers nor transducers are in any form aware of their concrete
input and output types.

#### Type simplification

```scala
type Reducer[A, R] = ((R, A) => R)

type Transducer[A, B] = (Reducer[B, R] => Reducer[A, R]) forSome { type R }
```

Reducers are just functions, that combine a result and an input value to a new result.
This implementation is restricted in such a way, that the `R` is identical (same `R` in and out).
This is mostly not a problem, since transducers may change types and most reducers have access
to a down-stream reducer of the target type (that can be different from `A`).
Reducers also have two additional operations.
The first is a setup operation, that is called with the current result before
and items flow through the reducer.
The second is a finish operation, that allows the reducer to do something whenever a stream of data is finished (although it might never finish).

A `Transducer[A, B]` is a function, that takes an `Reducer[B, _]` and returns an `Reducer[A, _]`.
Transducers transform their reducers from right to left. When finally applied, the data flows
from left to right. So, a `Transducer[A, B]` can roughly be seen as a function `A => B`, in that
it describes a transformation from `A` to `B`, but it is not specified, how many `B` can be produced.
A transducer may provide 0, 1, or an arbitrary number of `B`s, not just one (as `A => B` would have to).

An important note is, that `Transducer[A, B]` is a rank-2 type to quantify `R` without
needing to know about it. This makes transducers completely unaware of their input or output.


## Some other projects with partially similar traits

Besides [Clojure](http://clojure.org/) and [transducers-java](https://github.com/cognitect-labs/transducers-java)
there exist a number of projects, that share some traits with `transducers-scala`.

### RxJava (RsScala)

Probably the most similar concept are RxJava's Operators.
Operators transform Subscribers where transducers transform reducers.
While these operators are bound to be used with Subscribers, the Subscriber/Observer/Observable
itself can be plugged in easily on different sources and targets.

### psp-view (psp-std)

Paul Phillips'
[take on a scala collection/standard library](https://github.com/paulp/psp-std/)
includes collections, that are thoroughly non-strict and thus, achieve the same traits on terms of
laziness and early termination. These are, however, regular collections where the operations are
bound to the specific collection.


## Acknowledgement

This stuff is build on top of Rich Hickeys transducers for Clojure and Java.
See [this talk](https://www.youtube.com/watch?v=6mTbuzafcII)
and [this post](http://blog.cognitect.com/blog/2014/8/6/transducers-are-coming) for more info.
Also, there is a [Java implementation](https://github.com/cognitect-labs/transducers-java).

The sbt launcher is provided by [Paul Phillips](https://github.com/paulp/sbt-extras).


## Stuff todo

- documentation
- some more tests
- contrib modules, that included
    - support for scalaz type classes
    - support for functionaljava classes
    - support for guava classes

## License

Copyright 2014 Paul Horn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
