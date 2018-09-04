package catsexamples

import cats.{Id, ~>}
import cats.free.Free
import cats.free.Free.liftF
import scala.collection.mutable

object FreeExample {
  // based on https://typelevel.org/cats/datatypes/freemonad.html

  // An ADT
  sealed trait KVStoreA[A]
  case class Put[T](key: String, value: T) extends KVStoreA[Unit]
  case class Get[T](key: String) extends KVStoreA[Option[T]]
  case class Delete(key: String) extends KVStoreA[Unit]

  // Free
  type KVStore[A] = Free[KVStoreA, A]

  // Smart constructors

  // Put returns nothing (i.e. Unit).
  def put[T](key: String, value: T): KVStore[Unit] =
    liftF[KVStoreA, Unit](Put[T](key, value))

  // Get returns a T value.
  def get[T](key: String): KVStore[Option[T]] =
    liftF[KVStoreA, Option[T]](Get[T](key))

  // Delete returns nothing (i.e. Unit).
  def delete(key: String): KVStore[Unit] =
    liftF(Delete(key))

  // Update composes get and set, and returns nothing.
  def update[T](key: String, f: T => T): KVStore[Unit] =
    for {
      vMaybe <- get[T](key)
      _ <- vMaybe.map(v => put[T](key, f(v))).getOrElse(Free.pure(()))
    } yield ()

  // A program
  def program: KVStore[Option[Int]] =
    for {
      _ <- put("wild-cats", 2)
      _ <- update[Int]("wild-cats", (_ + 12))
      _ <- put("tame-cats", 5)
      n <- get[Int]("wild-cats")
      _ <- delete("tame-cats")
    } yield n

  // What does a program look like?
  //
  // for {
  //   _ <- put("wild-cats", 2)
  //   _ <- put("tame-cats", 5)
  //   n <- get[Int]("wild-cats")
  //   _ <- delete("tame-cats")
  // } yield n
  //
  //              |
  //              |
  //              V
  //
  // put("wild-cats", 2).flatMap(_ =>
  //   put("tame-cats", 5).flatMap(_ =>
  //     get[Int]("wild-cats").flatMap(n =>
  //       delete("tame-cats").map(_ =>
  //         n))))
  //
  //              |
  //              |
  //              V
  //
  // FlatMapped(put("wild-cats", 2), _ =>
  //   FlatMapped(put("tame-cats", 5), _ =>
  //     FlatMapped(get[Int]("wild-cats"), n =>
  //       FlatMapped(delete("tame-cats"), _ =>
  //         Pure(n)))))
  //
  //              |
  //              |
  //              V
  //
  // FlatMapped(Suspend(Put("wild-cats", 2)), _ =>
  //   FlatMapped(Suspend(Put("tame-cats", 5)), _ =>
  //     FlatMapped(Suspend(Get[Int]("wild-cats")), n =>
  //       FlatMapped(Suspend(Delete("tame-cats")), _ =>
  //         Pure(n)))))
  //
  
  // An interpreter
  def impureInterpreter: KVStoreA ~> Id  =
    new (KVStoreA ~> Id) {
    // a very simple (and imprecise) key-value store
    val kvs = mutable.Map.empty[String, Any]

    def apply[A](fa: KVStoreA[A]): Id[A] =
      fa match {
        case Put(key, value) =>
          println(s"put($key, $value)")
          kvs(key) = value
          ()
        case Get(key) =>
          println(s"get($key)")
          kvs.get(key).map(_.asInstanceOf[A])
        case Delete(key) =>
          println(s"delete($key)")
          kvs.remove(key)
          ()
      }
    }

  // We could make other interpreters
  // KvStoreA ~> Future - asynchronous computation
  // KvStoreA ~> Either - computation with fail-fast error handling

  // foldMap: (Free[S, A], S ~> M) => M[A]

  val result1: Id[Option[Int]] = program.foldMap(impureInterpreter)
  val result2: Option[Int] = program.foldMap(impureInterpreter)
}
