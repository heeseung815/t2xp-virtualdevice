package ai.t2x.virtualdevice.devd.mqtt

import ai.t2x.lib.common.mqtt.MqttMessageHandler
import ai.t2x.virtualdevice.devd.actor.OutboundPublish
import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttMessage}

/**
  * 2018. 6. 21. - Created by Cho, Hee-Seung
  */
class MqttMessageHandlerImpl(actor: ActorRef) extends MqttMessageHandler with StrictLogging {


  override def processForPub(token: IMqttDeliveryToken): Unit = ???

  override def processForSub(topic: String, message: MqttMessage): Unit = {
    logger.info("# processForSub.......!!!")
    if (MqttTopics.SYS_REQUESTORS_ALL.equalsIgnoreCase(topic.split("/").dropRight(1).mkString("/"))) {
      logger.info("# PromiseTest Topic!")
      actor ! OutboundPublish(topic, message.getPayload.map(_.toChar).mkString)
    } else {
      logger.info("# Other Topic!")
    }
  }
}
