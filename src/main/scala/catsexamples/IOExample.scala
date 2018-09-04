package catsexamples

import cats.effect.IO

object IOExample {
  val sayMoshi = IO { println("moshi") }

  // an action that will do some IO (and return nothing)
  val pickupTelephone: IO[Unit] =
    for {
      _ <- sayMoshi
      _ <- sayMoshi
    } yield ()

  pickupTelephone.unsafeRunSync()

  val askForInt: IO[Unit] = IO { println("Please type in an integer") }
  val readInt: IO[Int] = IO { scala.io.StdIn.readLine().toInt }

  // an action that will return an Int after doing some IO
  val sumInputs: IO[Int] =
    for {
      _ <- askForInt
      a <- readInt
      _ <- askForInt
      b <- readInt
    } yield a + b

  val result = sumInputs.unsafeRunSync()

  // pure values
  // single place in program with side effects (calling unsafeRunSync)
  // methods dealing with IO are marked via types
}
