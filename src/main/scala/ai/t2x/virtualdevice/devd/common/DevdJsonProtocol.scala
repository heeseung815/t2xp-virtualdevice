package ai.t2x.virtualdevice.devd.common
/*
import ai.t2x.lib.common.mqtt.model.{MqttMsg, MqttMsgBody, MqttMsgHeader, MqttMsgWithoutHeader}
import ai.t2x.virtualdevice.devd.actor.ResponsibleMessage
*/
import spray.json._

import scala.math.BigDecimal.RoundingMode

/**
  * 2018. 6. 19. - Created by Cho, Hee-Seung
  */
object DevdJsonProtocol extends DefaultJsonProtocol {
  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case v: Int => JsNumber(v)
      case v: Long => JsNumber(v)
      case v: Float => JsNumber(BigDecimal(v).setScale(2, RoundingMode.HALF_UP).toFloat)
      case v: Double => JsNumber(BigDecimal(v).setScale(2, RoundingMode.HALF_UP).toDouble)
      case v: String => JsString(v)
      case v: Boolean if v => JsTrue
      case v: Boolean if !v => JsFalse
      case v: Array[Int] => v.toJson
      case v: Array[Long] => v.toJson
      case v: Array[Double] => v.toJson
    }
    def read(value: JsValue) = value match {
      case JsNumber(n) if n.isValidInt => n.intValue()
      case JsNumber(n) if n.isValidLong => n.longValue()
      case JsNumber(n) if n.isDecimalFloat => n.floatValue()
      case JsNumber(n) if n.isDecimalDouble => n.doubleValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
      case _ => value
    }
  }
/*
  implicit val mqttMsgData4TelemetryFormat = jsonFormat6(MqttMsgData4Telemetry)
  implicit val mqttMsgBodyFormat = jsonFormat2(MqttMsgBody)
  implicit val mqttMsgHeaderFormat = jsonFormat2(MqttMsgHeader)
  implicit val mqttMsgWithoutHeaderFormat = jsonFormat1(MqttMsgWithoutHeader)
  implicit val mqttMsgFormat = jsonFormat2(MqttMsg)
*/
  implicit val deviceReqModelFormat = jsonFormat1(DeviceReqModel)
  implicit val deviceResModelFormat = jsonFormat3(DeviceResModel)

  implicit val telemetryValueModel = jsonFormat1(TelemetryValueModel)

/*
  implicit val reposibleMessageFormat = jsonFormat2(ResponsibleMessage)
*/
}

case class MqttMsgData4Telemetry(deviceId: String, deviceToken: String, deviceTokenType: Int, telemetryName: String, telemetryValue: Any, timestamp: Long)

case class DeviceReqModel(deviceId: String)
case class DeviceResModel(deviceId: String, deviceToken: String, deviceTokenType: Int = 0)

case class TelemetryValueModel(value: Any)