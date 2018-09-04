package catsexamples

import cats.data.Reader

object ReaderExample {
  // a value of type String that depends on a parameter of type String
  val a: Reader[String, String] = Reader(name => s"Hello $name")
  // (pretty much just a function)

  // val a = f(conf)
  // val b = g(conf)
  // val c = h(conf)
  //
  //       |
  //       |
  //       V
  //
  // (for {
  //   a <- f
  //   b <- g
  //   c <- h
  //  }).run(conf)
  //
  // the effect of the monad:
  // passing a parameter of type T

  val url = "dbhost:1234"

  object Before {
    def write(databaseUrl: String)(key: String, value: Int): Unit = ???

    def read(databaseUrl: String)(key: String): Int = ???

    def getAndInc(url: String)(key: String): Int = {
      val result = read(url)(key)
      write(url)(key, result + 1)
      result
    }

    val result: Int = getAndInc(url)("abc")
  }

  object After {
    def write(key: String, value: Int): Reader[String, Unit] = Reader { url =>
      ???
    }

    def read(key: String): Reader[String, Int] = Reader { url =>
      ???
    }

    def getAndInc(key: String): Reader[String, Int] =
      for {
        result <- read(key)
        _ <- write(key, result + 1)
      } yield result

    val result: Int = getAndInc("abc").run(url)
  }
}
