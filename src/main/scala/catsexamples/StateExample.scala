package catsexamples

import cats.data.State

object StateExample {
  // a value of type String that depends on (and can optionally modify) a state of type Int
  val a: State[Int, String] = State(s => (s + 1, "hello"))

  // val (s2, r1) = f(s1)
  // val (s3, r2) = g(s2)
  // val (s4, r3) = h(s3)
  //
  //         |
  //         |
  //         V
  //
  // (for {
  //    r1 <- f
  //    r2 <- g
  //    r3 <- h
  //  }.run(s1).value
  //
  // the effect of the monad:
  // passing (and optionally modifying) state of type S

  // example from https://typelevel.org/cats/datatypes/state.html

  case class Robot(
    id: Long,
    sentient: Boolean,
    name: String,
    model: String
  )

  case class Seed(long: Long) {
    def next = Seed(long * 6364136223846793005L + 1442695040888963407L)
  }

  val initialSeed = Seed(13L)

  object Before {
    def nextBoolean(seed: Seed): (Seed, Boolean) =
      (seed.next, seed.long >= 0L)

    def nextLong(seed: Seed): (Seed, Long) =
      (seed.next, seed.long)

    def createRobot(seed: Seed): Robot = {
      val (seed1, id) = nextLong(seed)
      val (seed2, sentient) = nextBoolean(seed1)
      val (seed3, isCatherine) = nextBoolean(seed2)
      val name = if (isCatherine) "Catherine" else "Carlos"
      val (seed4, isReplicant) = nextBoolean(seed3)
      val model = if (isReplicant) "replicant" else "borg"
      Robot(id, sentient, name, model)
    }

    val robot = createRobot(initialSeed)
  }

  object After {
    val nextBoolean: State[Seed, Boolean] = State { seed =>
      (seed.next, seed.long >= 0L)
    }

    val nextLong: State[Seed, Long] = State { seed =>
      (seed.next, seed.long)
    }

    val createRobot: State[Seed, Robot] =
      for {
        id <- nextLong
        sentient <- nextBoolean
        isCatherine <- nextBoolean
        name = if (isCatherine) "Catherine" else "Carlos"
        isReplicant <- nextBoolean
        model = if (isReplicant) "replicant" else "borg"
      } yield Robot(id, sentient, name, model)

    val robot = createRobot.runA(initialSeed).value
  }
}
