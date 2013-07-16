POC for better exception reporting for futures (macro-based)

When futures fail exceptions often contain too little context to determine the path of
composition leading to the error. This is a proof-of-concept how to use static source code 
context info gathered through a macro (basically a simple implementation of SIP-19 with macros)
to annotate exceptions with the composition path.

h2.Example


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
    case e =>
      throw new RuntimeException("Blubber")
      println("Finished with: "+e)
  }

  Thread.sleep(5000)
}
```

h2.Example output

```
net.virtualvoid.futures.RichFutureException: Exception 'Blubber' in future called through this stack: 
CallInfo(CallSite(net.virtualvoid.futures.FutureTest,None,FutureTest.scala,26),onComplete)
CallInfo(CallSite(net.virtualvoid.futures.FutureTest.Transformer,Some(div0),FutureTest.scala,13),flatMap)
CallInfo(CallSite(net.virtualvoid.futures.FutureTest,None,FutureTest.scala,19),RichFuture())

  at net.virtualvoid.futures.RichFuture$$anonfun$addInfo$1.apply(RichFuture.scala:51)
	at net.virtualvoid.futures.RichFuture$$anonfun$addInfo$1.apply(RichFuture.scala:49)
	at net.virtualvoid.futures.RichFuture$$anonfun$onComplete$1.apply(RichFuture.scala:28)
	at net.virtualvoid.futures.RichFuture$$anonfun$onComplete$1.apply(RichFuture.scala:25)
	at scala.concurrent.impl.CallbackRunnable.run(Promise.scala:29)
	at scala.concurrent.impl.ExecutionContextImpl$$anon$3.exec(ExecutionContextImpl.scala:107)
	at scala.concurrent.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260)
	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.pollAndExecAll(ForkJoinPool.java:1253)
	at scala.concurrent.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1346)
	at scala.concurrent.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979)
	at scala.concurrent.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107)
Caused by: java.lang.RuntimeException: Blubber
	at net.virtualvoid.futures.FutureTest$$anonfun$2.apply(FutureTest.scala:28)
	at net.virtualvoid.futures.FutureTest$$anonfun$2.apply(FutureTest.scala:26)
	at net.virtualvoid.futures.RichFuture$$anonfun$onComplete$1.apply(RichFuture.scala:26)
	... 8 more
```

