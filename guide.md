
Transducers are a way to build reusable transformations.

Let's start with some input data `xs`, that will be used for further examples:


```
(1 to 10).toList
```

`> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)`

And we will use this simple transducer `tx`, that will filter out odd numbers:


```
transducers.filter((x: Int) ⇒ x % 2 == 0)
```

`> (filter)`

### Decoupling from in- and output


A transducer is independent from its source or target. Notice how the
definition of the transducer did not involve anything from the source
or the target.


If you `run` the transducer, it will use the input shape for the output.

+ `e1`



```
transducers.run(tx)(xs)
```

`> List(2, 4, 6, 8, 10)`


You can also change the output shape using `into`:

+ `e2`



```
transducers.into[Vector].run(tx)(xs)
```

`> Vector(2, 4, 6, 8, 10)`


The shape has to be a first-order kinded type, i.e. `F[_]` or `* -> *`.
There must be an instance of `AsTarget[F]` available. For some types,
there is already an instance available.


```
(transducers.into[List].run(tx)(xs)
, transducers.into[Vector].run(tx)(xs)
, transducers.into[Stream].run(tx)(xs)
, transducers.into[Set].run(tx)(xs)
, transducers.into[Iterator].run(tx)(xs)
, transducers.into[Iterable].run(tx)(xs)
, transducers.into[Option].run(tx)(xs)
, transducers.into[Option](AsTarget.lastOption).run(tx)(xs)
)
```

`> (List(2, 4, 6, 8, 10),Vector(2, 4, 6, 8, 10),Stream(2, ?),Set(10, 6, 2, 8, 4),non-empty iterator,List(2, 4, 6, 8, 10),Some(2),Some(10))`


Note that choosing `Stream` won't automatically stop the consumption of the
input, when the stream is not consumed. Also, using `Option` will not
terminate the input consumption early. This might change in the future
(PR welcome ;-) ).


If you already have some target data available, you can use `addto`:

+ `e3`



```
val result = (-10 to 0 by 2).toVector
transducers.addto(result).run(tx)(xs)
```

`> Vector(-10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10)`


The same things about `AsTarget` apply for `addto` as they do for `into`.


All three methods also support a variant where the arguments are reversed
(`(input)(transducer)`) which works better with type inference, if the
transducer is declared inline:


```
transducers.run(transducers.filter((x: Int) ⇒ x % 2 == 0))(xs)
//  transducers.run(transducers.filter(_ % 2 == 0))(xs)  // wouldn't compile
transducers.run(xs)(transducers.filter(_ % 2 == 0))
```

`> List(2, 4, 6, 8, 10)`


`into` and `addto` also have a `from` method, where you can define the input
shape, and a transducer.
You get a function from input to output:

+ `e4`



```
val fn: List[Int] ⇒ Vector[Int] = transducers.into[Vector].from[List].run(tx)
fn(xs)
```

`> Vector(2, 4, 6, 8, 10)`


### Composing Transducers


Transducers can be composed as if they were functions (a `Transducer[A, B]`
is basically just a `Reducer[B, _] ⇒ Reducer[A, _]`).


You can use `compose` to create a new Transducer:

+ `e5`



```
val tx2 = transducers.map((_: Int) * 5)
val tx0 = tx compose tx2 // first map (*5), then filter (even?)

transducers.run(tx0)(xs)
```

`> List(10, 20, 30, 40, 50)`


You can also use `andThen`

+ `e6`



```
val tx2 = transducers.map((_: Int) * 4)
val tx0 = tx andThen tx2 // first filter (even?), then map (*4)
transducers.run(tx0)(xs)
```

`> List(8, 16, 24, 32, 40)`


`>>` is an alias for `andThen`

+ `e7`



```
val tx2 = transducers.map((_: Int) * 4)
val tx0 = tx >> tx2 // first filter (even?), then map (*4)
transducers.run(tx0)(xs)
```

`> List(8, 16, 24, 32, 40)`


Instead of creating the transducers beforehand and fighting the
type-inference, you directly chain transducers by calling the corresponding
methods.


This way, the API looks like that of a collection type

+ `e8`



```
val tx0 = tx map (4 *) map (2 +) drop 4
transducers.run(tx0)(xs)
```

`> List(42)`


### Laziness and early termination


Transducers are inherently lazy in their execution, similar to views. Unlike
in normal collections, every step is executed in every transducer before the
next value is computed.


To demonstrate this, the input is wrapped in an iterator `it`, that counts
how often it was consumed


```
CountingIterator(xs)
```

`> non-empty iterator`


So, take terminates early if everything is taken

+ `e9`



```
val tx0 = tx.take(2)
(transducers.into[List].run(tx0)(it.it), it.consumed)
```

`> (List(2, 4),4)`


Here, only 4 items were consumed (out of 10), because after 4 items, 2 were
found that matched the filter and the process could be terminated.


Another effect of lazy evaluation is, that transducers can operate on
infinite collections where the default scala collection operators would fail
to terminate.


Consider this example in Scala:


```
Stream.from(1).dropRight(5).take(5)
// java.lang.OutOfMemoryError: GC overhead limit exceeded
```



This works with transducers

+ `e10`



```
val xs = Stream.from(1)
val it = CountingIterator(xs)
val tx0 = tx.dropRight(5).take(5)

(transducers.into[List].run(tx0)(it.it), it.consumed)
```

`> (List(2, 4, 6, 8, 10),20)`


20 Items are consumed, 10 of which pass the filter condition; 5 are buffered
in case they need to be dropped and 5 will be taken until the process
terminates.

