# Cassandra Datastax Driver - Scala Integration

The project is based on numerous articles touching upon the topic of integration Cassandra Datastax driver 
with Scala. 
I was fed up copy-pasting those extra classes into all my projects.
This is an open-source solution to that problem.

The integration is divided into two parts.

- [`CQL` and *query execution* integration](#cassandra-scala-integration)
- [ListenableFuture Scala Integration](#listenablefuture-scala-integration)

# Requirements

Minimal supported `cassandra-driver-core` version is `3.2.0`. <br> 
Previous versions were based on `guava 16.0.1` which was really difficult to use.

# Cassandra Scala Integration

Project `cassandra-scala` allows for easier cassandra operations providing with `CQL`.

Add `import com.github.atais.cassandra.ScalaIntegration._` to use it.
 
Example usage:

```scala
implicit protected val session: com.datastax.driver.core.Session
implicit protected val executor: java.util.concurrent.Executor

val selectCQL: ListenableFuture[PreparedStatement] = cql"SELECT * FROM $table WHERE key = ?"
val result: ListenableFuture[ResultSet] = execute(selectCQL, "my-key")

result.map(rs => rs.one())
      .map(...)
```

## Available methods description:

### cql

Asynchronously create `PreparedStatement` for later reuse.

```scala
cql"SELECT * FROM $table WHERE key = ?"
```

### execute

Asynchronously execute `PreparedStatement` passing parameters.

```scala
ScalaIntegration.execute(cql"SELECT * FROM $table WHERE key = ?", "testValue")
```

# ListenableFuture Scala Integration

Project `listenable-future-scala` allows for *Scala-like* handling of [`ListenableFuture`](https://github.com/google/guava/wiki/ListenableFutureExplained).

Add `import com.github.atais.listenablefuture.ListenableFutureScala._` to use it.

## Available methods description:

### map

```scala
Futures.immediateFuture("abc")
       .map(text => text.length)
       .map(i    => i + 1)
```

### flatMap

```scala
Futures.immediateFuture("abc")
       .flatMap(text => Futures.immediateFuture(text.length))
       .flatMap(i    => Futures.immediateCancelledFuture())
```

### for-comprehension support

```scala
for {
  text   <- Futures.immediateFuture("abc")
  number <- Futures.immediateFuture(123)
} yield {
  text + number
}
```

### recover

`scala.concurrent.Future` uses `PartialFunction` to handle `recover` cases, however it is not possible with `Guava`.
Instead one has to pass the `Throwable` class as function parameter matching with handling function.

```scala
Futures.immediateFuture("abc")
       .map(_ => throw new UnsupportedOperationException)
       .recover[UnsupportedOperationException] {
         _: UnsupportedOperationException => "it's fine"
       }
```

### recoverWith

Has to be used similarly to [recover](#recover), however function `T => ListenableFuture[A]` has to be provided.

```scala
Futures.immediateFuture("abc")
       .map(_ => throw new UnsupportedOperationException)
       .recoverWith[UnsupportedOperationException] {
         _: UnsupportedOperationException => Futures.immediateFuture("it's fine")
       }
```

### future

Converts to `scala.concurrent.Future` 

```scala
Futures.immediateFuture("abc")
       .future
```

# License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
