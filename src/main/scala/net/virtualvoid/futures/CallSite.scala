package net.virtualvoid.futures

import scala.reflect.macros.Context

case class CallSite(
  enclosingClass: String,
  enclosingMethod: Option[String],
  file: String,
  line: Int)

object CallSite {
  import language.experimental.macros

  implicit def callSite: CallSite = macro CallSiteMacro.callSiteImpl
}

object CallSiteMacro {
  def callSiteImpl(c: Context): c.Expr[CallSite] = {
    import c._
    import universe._

    val method =
      scala.util.Try(enclosingMethod.symbol.name.decoded).toOption match {
        case Some(s) => reify(Some(literal(s).splice))
        case None => reify(None)
      }

    reify {
      CallSite(
        literal(enclosingClass.symbol.fullName).splice,
        method.splice,
        literal(enclosingPosition.source.file.name).splice,
        literal(enclosingPosition.line).splice)
    }
  }
}
