POC for better exception reporting for futures (macro-based)

When futures fail exceptions often contain too little context to determine the path of
composition leading to the error. This is a proof-of-concept how to use static source code 
context info gathered through a macro (basically a simple implementation of SIP-19 with macros)
to annotate exceptions with the composition path.

Example
-------


```scala
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
    case e => println("Finished with: "+e)
  }

  Thread.sleep(5000)
}
```

prints


```
Finished with: Failure(net.virtualvoid.futures.RichFutureException: Exception '/ by zero' in future called through this stack: 
CallInfo(CallSite(net.virtualvoid.futures.FutureTest.Transformer,Some(div0),FutureTest.scala,13),flatMap)
CallInfo(CallSite(net.virtualvoid.futures.FutureTest,None,FutureTest.scala,19),RichFuture())
)
```

