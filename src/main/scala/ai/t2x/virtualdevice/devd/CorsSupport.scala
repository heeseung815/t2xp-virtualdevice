package ai.t2x.virtualdevice.devd

import akka.http.scaladsl.model.HttpMethods.{DELETE, OPTIONS, PATCH, PUT}
import akka.http.scaladsl.model.headers.{Origin, `Access-Control-Allow-Credentials`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{HttpHeader, HttpResponse}
import akka.http.scaladsl.server.Directives.{complete, handleRejections, mapInnerRoute, respondWithHeaders}
import akka.http.scaladsl.server.{Directive0, MethodRejection, RejectionHandler}

/**
  * 2018. 6. 18. - Created by Cho, Hee-Seung
  */
trait CorsSupport {

  protected def corsAllowOrigins: List[String]

  protected def corsAllowedHeaders: List[String]

  protected def corsAllowCredentials: Boolean

  protected def optionsCorsHeaders: List[HttpHeader]

  protected def corsRejectionHandler(allowOrigin: `Access-Control-Allow-Origin`) = RejectionHandler
    .newBuilder().handle {
    case MethodRejection(supported) =>
      complete(HttpResponse().withHeaders(
        `Access-Control-Allow-Methods`(OPTIONS, DELETE, PATCH, PUT, supported) ::
          allowOrigin ::
          optionsCorsHeaders
      ))
  }
    .result()

  private def originToAllowOrigin(origin: Origin): Option[`Access-Control-Allow-Origin`] =
    if (corsAllowOrigins.contains("*") || corsAllowOrigins.contains(origin.value))
      origin.origins.headOption.map(`Access-Control-Allow-Origin`.apply)
    else
      None

  def cors[T]: Directive0 = mapInnerRoute { route => context =>
    ((context.request.method, context.request.header[Origin].flatMap(originToAllowOrigin)) match {
      case (OPTIONS, Some(allowOrigin)) =>
        handleRejections(corsRejectionHandler(allowOrigin)) {
          respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials)) {
            route
          }
        }
      case (_, Some(allowOrigin)) =>
        respondWithHeaders(allowOrigin, `Access-Control-Allow-Credentials`(corsAllowCredentials)) {
          route
        }
      case (_, _) =>
        route
    })(context)
  }
}