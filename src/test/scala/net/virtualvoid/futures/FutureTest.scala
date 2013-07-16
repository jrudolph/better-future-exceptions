package net.virtualvoid.futures

import scala.concurrent.ExecutionContext



object FutureTest extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  //import scala.concurrent.{ Future => RichFuture }

  object Transformer {
    def div0(f: RichFuture[Int])(implicit ctx: ExecutionContext): RichFuture[Int] =
      f.flatMap { i =>
        val j = i / 0
        RichFuture { Thread.sleep(1000); j + 12 } }
  }


  val f = RichFuture {
    Thread.sleep(2000)
    42
  }

  val res = Transformer.div0(f)

  res.onComplete {
    case e =>
      throw new RuntimeException("Blubber")
      println("Finished with: "+e)
  }

  Thread.sleep(5000)
}

