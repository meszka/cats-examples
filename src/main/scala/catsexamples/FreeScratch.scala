object FreeScratch {
  // def printFree[S[_], A](free: Free[S, A]): String =
  //   free match {
  //     case Pure(a) => s"Pure(${a.toString})"
  //     case Suspend(sa) => s"Suspend(${sa.toString})"
  //     case FlatMapped(c, f) => s"FlatMapped(${c.toString}, ${f.toString})"
  //   }


  //  def foldMap[M[_]](f: FunctionK[S, M])(implicit M: Monad[M]): M[A] =
  //    M.tailRecM(this)(_.step match {
  //      case Pure(a) => M.pure(Right(a))
  //      case Suspend(sa) => M.map(f(sa))(Right(_))
  //      case FlatMapped(c, g) => M.map(c.foldMap(f))(cc => Left(g(cc)))
  //    })
  //
  //                :
  //                :
  //                V
  //
  //  def foldMap[M[_]](f: FunctionK[S, M])(implicit M: Monad[M]): M[A] =
  //    M.flatMap(this)(_ match  {
  //      case Pure(a) => M.pure(a)
  //      case Suspend(sa) => f(sa)
  //      case FlatMapped(c, g) => (M.map(c.foldMap(f))(g)).foldMap(f)
  //    })
  //
  //    foldMap: (Free[S, A], S ~> M) => M[A]
}
