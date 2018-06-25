package ai.t2x.virtualdevice.devd.common

import ai.t2x.virtualdevice.devd.actor.{RequestManagerActor, RequestMessage, RequestMessageWithPromise}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future, Promise}
import akka.pattern.ask

import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * 2018. 6. 21. - Created by Cho, Hee-Seung
  */
class Requestor(implicit actorSystem: ActorSystem) {
  val actor: ActorRef = actorSystem.actorOf(Props(new RequestManagerActor), RequestManagerActor.actorName)

  def request[T](topic: String, msg: Any)(implicit ec: ExecutionContext, timeout: Timeout): Future[T] = {
    val p: Promise[T] = Promise[T]()

    /*
    val f = actor ? RequestMessage(topic, msg)

    f.onComplete {
      case Success(rsp) =>
        p.success(rsp.asInstanceOf[T])
      case Failure(ex) if ex.isInstanceOf[Throwable]=>
        p failure ex.asInstanceOf[Throwable]
      case m =>
        p.failure(new RuntimeException("Unknown error: " + m.toString))
    }
    */

    actor ! RequestMessageWithPromise[T](topic, msg, p)
    p.future
  }
}

object Requestor {
  def apply(implicit actorSystem: ActorSystem): Requestor = new Requestor()
}
