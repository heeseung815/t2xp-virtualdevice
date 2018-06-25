package ai.t2x.virtualdevice.devd.actor

import ai.t2x.lib.common.mqtt.MqttWebSocketPublisher
import ai.t2x.lib.common.mqtt.model.{MqttMsgBody, MqttMsgWithoutHeader}
import ai.t2x.virtualdevice.devd.actor.SchedulerActor.{PublishMqttMessage, ScheduleSetting}
import ai.t2x.virtualdevice.devd.common.DevdJsonProtocol._
import ai.t2x.virtualdevice.devd.common.{DevdConfig, MqttMsgData4Telemetry}
import akka.actor.{Actor, Cancellable}
import com.typesafe.scalalogging.StrictLogging
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

/**
  * 2018. 6. 20. - Created by Cho, Hee-Seung
  */
class SchedulerActor extends Actor with StrictLogging {
  private var count = 0
  private var cancellable: Cancellable = _
  private var isScheduleOnce = false

  private val topic = "create/telemetry"
  private val qos = 2

//  private val deviceId = DevdConfig.config.getString("repod.deviceEntityId")
//  private val deviceToken = deviceId.split("-").dropRight(1).mkString
  private val deviceTokenType = 0

  private val telemetryList = Array("noise", "temperature", "vibration")
  override def preStart(): Unit = {
    logger.info(s"SchedulerActor started. actorPath=${self.path}")
  }

  override def receive: Receive = {
    case ScheduleSetting(deviceId, telemetryName, interval, count, publisher) =>
      logger.info(s"# Received ScheduleSetting message. telemetryName=$telemetryName, interval=$interval, count=$count")
      this.count = count
      cancellable = context.system.scheduler.schedule(0 seconds, interval seconds, self, PublishMqttMessage(publisher, deviceId, telemetryName))
    case PublishMqttMessage(publisher, deviceId, telemetryName) =>
      logger.info("# Received PublishMqttMessage message.")

      if (count > 0) {
        val rand = new Random(System.currentTimeMillis())
        //        val random_index = rand.nextInt(telemetryList.length)
        //        val telemetryName = telemetryList(random_index)
        val telemetryValue: Any = telemetryName match {
          case "vibration" => Random.nextInt(100)
          case "temperature" | "noise" => between(10.00, 100.00, rand)
        }

        logger.info(s">>> telemetryName=$telemetryName, telemetryValue=$telemetryValue")
        val o = MqttMsgWithoutHeader(MqttMsgBody("device_telemetry", MqttMsgData4Telemetry(deviceId, deviceId.split("-").dropRight(1).mkString, deviceTokenType, telemetryName, telemetryValue, System.currentTimeMillis()).toJson))
        val message = o.toJson.compactPrint
        publisher.pub(topic, message.getBytes(), qos, null)

        count -= 1
      } else {
        if (!cancellable.isCancelled) cancellable.cancel()
        count = 0
        logger.info("Succeed to send all scheduled message.")
      }
  }

  private def between(low: Double, high: Double, r: Random): Double = {
    if (low == high) {
      low
    } else {
      val mid = low + (high/2 - low/2)
      if (r.nextBoolean) between(low, mid, r) else between(mid, high, r)
    }
  }
}

object SchedulerActor {
  case class ScheduleSetting(deviceId: String, telemetryName: String, interval: Int, count: Int, publisher: MqttWebSocketPublisher)
  case class PublishMqttMessage(publisher: MqttWebSocketPublisher, deviceId: String, telemetryName: String)
}