package catsexamples

object MonadExample {
  val anOption: Option[Int] = Option(4)

  val anotherOption: Option[Int] = anOption.map(_ + 1)

  val andAnother: Option[Int] = anOption.flatMap(a => if (a % 2 == 0) Some(x + 1) else None)

  val sum: Option[Int] = for {
    a <- anOption
    b <- anotherOption
    c <- andAnother
  } yield (a + b + c)

  val sum1: Option[Int] =
    anOption.flatMap(a =>
      anotherOption.flatMap(b =>
        andAnother.map(c =>
          a + b + c
        )
      )
    )

  // a value with an additional effect
  //
  // in this case the effect is optionality
  //
  // a monad's methods / for expressions allow sequencing actions
  // each next action depends on the previous one
}
