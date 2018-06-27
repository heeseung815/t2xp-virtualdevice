package ai.t2x.virtualdevice.devd.api.controller
/*
import ai.t2x.lib.common.mqtt.MqttWebSocketPublisher
import ai.t2x.lib.common.mqtt.model.{MqttMsgBody, MqttMsgWithoutHeader}
import ai.t2x.virtualdevice.devd.actor.SchedulerActor
import ai.t2x.virtualdevice.devd.actor.SchedulerActor.ScheduleSetting
*/
import ai.t2x.virtualdevice.devd.common.DevdJsonProtocol._
import ai.t2x.virtualdevice.devd.common._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.StrictLogging
import io.swagger.annotations._
import javax.ws.rs.Path
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * 2018. 6. 19. - Created by Cho, Hee-Seung
  */
@Api(value = "Virtual Device", produces = "application/json")
@Path("/virtualdevice")
class VirtualDeviceCtrl(scheduler: ActorRef)(implicit actorSystem: ActorSystem) extends Directives with StrictLogging {
  private val broker = DevdConfig.config.getString("devd.mqtt.broker")
  private val publisherClientId = DevdConfig.config.getString("devd.mqtt.publisher.clientId")
  private val topic = "create/telemetry"
  private val qos = 2

//  private val deviceId = DevdConfig.config.getString("devd.deviceEntityId")
//  private val deviceToken = deviceId.split("-").dropRight(1).mkString
  private val deviceTokenType = 0

//  lazy val publisher: MqttWebSocketPublisher = MqttWebSocketPublisher(broker, publisherClientId)

  def route: Route = sendTelemetry ~ sendRandomTelemetry ~ createDevice ~ check

  def check: Route =
    path("virtualdevice" / "check") {
      get {
        logger.info("VirtualDevice Health Check.")
        complete("AWS EC2 Test!!")
      }
    }

  @Path("/create")
  @ApiOperation(code = 201, value = "Create Device", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "body", value = "Telemetry Object", required = true, dataType = "ai.t2x.virtualdevice.devd.common.DeviceReqModel", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Entity Created"),
    new ApiResponse(code = 400, message = "Bad Request"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def createDevice: Route =
    path("virtualdevice" / "create") {
      post {
        entity(as[DeviceReqModel]) { o: DeviceReqModel =>
          logger.info("### devd createDevice api called.")

          val httpRequest = HttpRequest(
            HttpMethods.POST,
            Uri("http://192.168.7.192:9002/authentication/device"),
            List(
              Accept(MediaRange(MediaTypes.`application/json`))
            ),
            HttpEntity(MediaTypes.`application/json`, o.toJson.compactPrint)
          )

          val responseFuture: Future[HttpResponse] = Http().singleRequest(httpRequest)
          onComplete(responseFuture) {
            case Success(_) =>
              logger.info("### authd api success")
              val result: DeviceResModel = DeviceResModel(o.deviceId, o.deviceId.split("-").dropRight(1).mkString)
              complete(result)
            case Failure(e) =>
              logger.error(e.getMessage)
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }

  @Path("/send/telemetry")
  @ApiOperation(code = 200, value = "Send Telemetry", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "deviceId", value = "Device Identifier", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "telemetryName", value = "Telemetry Name", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "body", value = "Telemetry Value Object", required = true, dataType = "ai.t2x.virtualdevice.devd.common.TelemetryValueModel", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def sendTelemetry: Route =
    path("virtualdevice" / "send" / "telemetry") {
      post {
        parameter('deviceId.as[String], 'telemetryName.as[String]) { (deviceId: String, telemetryName: String) =>
          entity(as[TelemetryValueModel]) { obj: TelemetryValueModel =>
            logger.info(s">>> telemetryName=$telemetryName, telemetryValue=${obj.value}")

//            val o = MqttMsgWithoutHeader(MqttMsgBody("device_telemetry", MqttMsgData4Telemetry(deviceId, deviceId.split("-").dropRight(1).mkString, deviceTokenType, telemetryName, obj.value, System.currentTimeMillis()).toJson))
//            val message = o.toJson.compactPrint
//            publisher.pub(topic, message.getBytes(), qos, null)

            complete(StatusCodes.OK)
          }
        }
      }
    }

  @Path("/send/randomTelemetry")
  @ApiOperation(code = 200, value = "Send Random Telemetry", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "deviceId", value = "Device Identifier", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "telemetryName", value = "Telemetry Name", required = true, dataType = "string", paramType = "query"),
    new ApiImplicitParam(name = "interval", value = "Interval (s)", required = true, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "count", value = "Count", required = true, dataType = "integer", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success"),
    new ApiResponse(code = 500, message = "Internal server error")
  ))
  def sendRandomTelemetry: Route =
    path("virtualdevice" / "send" / "randomTelemetry") {
      get {
        parameter('deviceId.as[String], 'telemetryName.as[String], 'interval.as[Int], 'count.as[Int]) { (deviceId: String, telemetryName: String, interval: Int, count: Int) =>
          logger.info(s">>> interval=$interval, count=$count")

//          scheduler ! ScheduleSetting(deviceId, telemetryName, interval, count, publisher)

          complete(StatusCodes.OK)
        }
      }
    }
}

object VirtualDeviceCtrl {
  def apply(scheduler: ActorRef)(implicit actorSystem: ActorSystem): VirtualDeviceCtrl = new VirtualDeviceCtrl(scheduler)
}