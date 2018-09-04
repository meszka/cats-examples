package catsexamples

import cats.data.Validated
import cats.implicits._
import play.api.libs.json._

object ValidatedExample {
  case class User(email: String, password: String)

  val validUser = Json.parse("""{"email": "user1@example.com", "password": "abcdefg123"}""")
  val userWithoutPassword = Json.parse("""{"name": "user1@example.com"}""")
  val blankUser = Json.parse("""{}""")

  // Fail fast error handling with Either
  def validateUser(json: JsValue): Either[String, User] =
    for {
      email <- (json \ "email").asOpt[String].toRight("email missing")
      password <- (json \ "password").asOpt[String].toRight("password missing")
    } yield User(email, password)

  // Accumulated errors with Validated
  def validateUser1(json: JsValue): Validated[List[String], User] =
    (
      (json \ "email").asOpt[String].toValid(List("email missing")),
      (json \ "password").asOpt[String].toValid(List("password missing"))
    ).mapN(User)

  // Error type must have a Semigroup instance (so we know how to combine values)

  // Applicative: compose independent actions
  // Monad: compose sequentially dependent actions

  // map2: (F[A], F[B], (A, B) => C) => F[C]
  // mapN: (F[A1], F[A2], ... F[AN])((A1, A2, ... AN) => B) => F[B]
}
