package net.virtualvoid.futures

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class CallInfo(site: CallSite, method: String)
case class RichFutureException(callInfos: List[CallInfo], underlying: Throwable) extends RuntimeException(underlying) {
  override def getMessage: String = {
    s"Exception '${underlying.getMessage}' in future called through this stack: \n"+
    callInfos.mkString("\n")+"\n"
  }
}

trait RichFuture[T] {
  import RichFuture.withInfo

  def flatMap[U](f: T => RichFuture[U])(implicit context: ExecutionContext, site : CallSite): RichFuture[U] =
    withInfo(underlying.flatMap(f.andThen(_.underlying)), site, "flatMap", infos)

  def map[U](f: T => U)(implicit context: ExecutionContext, site: CallSite): RichFuture[U] = ???
  def foreach(f: T => Unit)(implicit context: ExecutionContext, site: CallSite): Unit = ???

  def onComplete[U](func: Try[T] => U)(implicit executor: ExecutionContext, site: CallSite): Unit = {
    val newHandler = RichFuture.addInfo(CallInfo(site, "onComplete") :: infos)
    underlying.onComplete { t =>
      try func(t)
      catch {
        case e => throw newHandler(e)
      }
    }
  }

  private[futures] def underlying: Future[T]
  private[futures] def infos: List[CallInfo]
}

case class RichFutureImpl[T](val underlying: Future[T], infos: List[CallInfo]) extends RichFuture[T]

object RichFuture {
  def apply[T](t: => T)(implicit context: ExecutionContext, site: CallSite): RichFuture[T] =
    withInfo(Future(t), site, "RichFuture()", Nil)

  private[futures] def withInfo[T](future: Future[T], site: CallSite, method: String, infos: List[CallInfo])(implicit context: ExecutionContext): RichFuture[T] = {
    val newInfos = CallInfo(site, method) :: infos
    new RichFutureImpl[T](future.transform(identity, addInfo(newInfos)), newInfos)
  }

  def addInfo(outerInfos: List[CallInfo]): Throwable => Throwable = {
    {
      case e@RichFutureException(infos, _) =>  e.copy(callInfos = infos ::: outerInfos)
      case e@_ => RichFutureException(outerInfos, e)
    }
  }
}
