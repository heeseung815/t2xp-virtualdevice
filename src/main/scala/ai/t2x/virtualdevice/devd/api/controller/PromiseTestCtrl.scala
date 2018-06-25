package ai.t2x.virtualdevice.devd.api.controller

import ai.t2x.virtualdevice.devd.common.Requestor
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * 2018. 6. 21. - Created by Cho, Hee-Seung
  */
class PromiseTestCtrl(requestor: Requestor)(implicit actorSystem: ActorSystem) extends Directives with StrictLogging {

  def route: Route = promiseTest

  def promiseTest: Route =
    path("promise" / "test") {
      get {
        implicit val timeout: Timeout = 3 second
        implicit val ec: ExecutionContext = actorSystem.dispatcher
        val f: Future[String] = requestor.request[String]("authentication/user/verify", "Hello")

        onComplete(f) {
          case Success(str) =>
            logger.info("Ok Responsed: {}", str)
            complete(str)
          case Failure(ex) =>
            logger.info("exception", ex)
            complete(StatusCodes.InternalServerError)
        }
      }
    }
}

object PromiseTestCtrl {
  def apply(requestor: Requestor)(implicit actorSystem: ActorSystem): PromiseTestCtrl = new PromiseTestCtrl(requestor)
}

