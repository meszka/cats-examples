package catsexamples

import cats.{Id, InjectK, ~>}
import cats.data.EitherK
import cats.free.Free
import scala.collection.mutable.ListBuffer

object InjectKExample {
  // based on https://typelevel.org/cats/datatypes/freemonad.html

  // Multiple ADTS

  // Handles user interaction
  sealed trait Interact[A]
  case class Ask(prompt: String) extends Interact[String]
  case class Tell(msg: String) extends Interact[Unit]

  // Represents persistence operations
  sealed trait DataOp[A]
  case class AddCat(a: String) extends DataOp[Unit]
  case class GetAllCats() extends DataOp[List[String]]

  // This type is a union of DataOp[A] and Interact[A]
  type CatsAppA[A] = EitherK[DataOp, Interact, A]

  // Free
  type CatsApp[A] = Free[CatsAppA, Unit]

  // Smart constructors
  
  // Constructors  of Interacts don't need to know about DataSource or CatsApp
  // InjectK[Interact, F] is evidence that Interact can be "cast up" to F (which will be CatsAppA)
  //
  // Fun fact: Inject[B, E] is evidence that B can be converted to E, where E is an Either,
  // e.g type E = Either[A, Either[B, C]]
  // (Inject[B, E] will be derived from E's type)
  //
  class Interacts[F[_]](implicit I: InjectK[Interact, F]) {
    def tell(msg: String): Free[F, Unit] = Free.inject[Interact, F](Tell(msg))
    def ask(prompt: String): Free[F, String] = Free.inject[Interact, F](Ask(prompt))
  }
  object Interacts {
    implicit def interacts[F[_]](implicit I: InjectK[Interact, F]): Interacts[F] = new Interacts[F]
  }

  class DataSource[F[_]](implicit I: InjectK[DataOp, F]) {
    def addCat(a: String): Free[F, Unit] = Free.inject[DataOp, F](AddCat(a))
    def getAllCats: Free[F, List[String]] = Free.inject[DataOp, F](GetAllCats())
  }
  object DataSource {
    implicit def dataSource[F[_]](implicit I: InjectK[DataOp, F]): DataSource[F] = new DataSource[F]
  }

  // A program using multiple ADTs
  def program(implicit I : Interacts[CatsApp], D : DataSource[CatsApp]): Free[CatsApp, Unit] = {
    import I._, D._

    for {
      cat <- ask("What's the kitty's name?")
      _ <- addCat(cat)
      cats <- getAllCats
      _ <- tell(cats.toString)
    } yield ()
  }

  // Interpreters

  object ConsoleCatsInterpreter extends (Interact ~> Id) {
    def apply[A](i: Interact[A]) = i match {
      case Ask(prompt) =>
        println(prompt)
        readLine()
      case Tell(msg) =>
        println(msg)
    }
  }

  object InMemoryDatasourceInterpreter extends (DataOp ~> Id) {
    private[this] val memDataSet = new ListBuffer[String]

    def apply[A](fa: DataOp[A]) = fa match {
      case AddCat(a) => memDataSet.append(a); ()
      case GetAllCats() => memDataSet.toList
    }
  }

  val interpreter: CatsApp ~> Id = InMemoryDatasourceInterpreter or ConsoleCatsInterpreter

  val evaled: Unit = program.foldMap(interpreter)
  // What's the kitty's name?
  // List(snuggles)
  // evaled: Unit = ()
}
