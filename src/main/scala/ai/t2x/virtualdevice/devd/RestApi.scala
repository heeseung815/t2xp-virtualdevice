package ai.t2x.virtualdevice.devd

import ai.t2x.virtualdevice.devd.actor.SchedulerActor
import ai.t2x.virtualdevice.devd.api.controller.{PromiseTestCtrl, VirtualDeviceCtrl}
import ai.t2x.virtualdevice.devd.api.swagger.SwaggerDocService
import ai.t2x.virtualdevice.devd.common.{DevdConfig, Requestor}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.headers.{Allow, `Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Max-Age`}
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.{Directives, MethodRejection, RejectionHandler, Route}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


/**
  * 2018. 6. 19. - Created by Cho, Hee-Seung
  */
class RestApi()(implicit actorSystem: ActorSystem) extends Directives with CorsSupport with StrictLogging {
  implicit val m: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  override val corsAllowOrigins: List[String] = List("*")
  override val corsAllowedHeaders: List[String] = List("Origin", "X-Requested-With", "Content-Type", "Accept", "Accept-Encoding", "Accept-Language", "Host", "Referer", "User-Agent")
  override val corsAllowCredentials: Boolean = true
  override val optionsCorsHeaders: List[HttpHeader] = List[HttpHeader](
    `Access-Control-Allow-Headers`(corsAllowedHeaders.mkString(", ")),
    `Access-Control-Max-Age`(60 * 60 * 24 * 20), // cache pre-flight response for 20 days
    `Access-Control-Allow-Credentials`(corsAllowCredentials)
  )

  private var bindingFuture: Future[ServerBinding] = _
  private var routes: Route = _

//  private val requestor = new Requestor

  private val scheduler: ActorRef = actorSystem.actorOf(Props(new SchedulerActor()))

  // Allow CORS
  def rejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder().handleAll[MethodRejection] { rejections =>
      val methods = rejections map (_.supported)
      lazy val names = methods map (_.name) mkString ", "

      respondWithHeader(Allow(methods)) {
        options {
          complete(s"Supported methods : $names.")
        } ~
          complete(StatusCodes.MethodNotAllowed, s"HTTP method not allowed, supported methods: $names!")
      }
    }.result()

  def start: Unit = {
    routes = handleRejections(rejectionHandler) {
      cors {
        SwaggerDocService.routes ~ VirtualDeviceCtrl(scheduler).route
//        SwaggerDocService.routes ~ VirtualDeviceCtrl(scheduler).route ~ PromiseTestCtrl(requestor).route
      }
    }

    logger.info("----------------------------")
    logger.info(s"host=${DevdConfig.host}")
    logger.info(s"port=${DevdConfig.port}")
    logger.info("----------------------------")

    bindingFuture = Http().bindAndHandle(routes, DevdConfig.host, DevdConfig.port)
    bindingFuture.onComplete {
      case Success(b: ServerBinding) => logger.info(s"Succeed to start Rest API Service for DEVD. Listening on: ${b.localAddress}")
      case Failure(e: Throwable) => logger.error(s"Failed to start Rest API Service for DEVD. error: ${e.getMessage}")
    }
  }
}

object RestApi {
  def apply()(implicit actorSystem: ActorSystem): RestApi = new RestApi()
}
