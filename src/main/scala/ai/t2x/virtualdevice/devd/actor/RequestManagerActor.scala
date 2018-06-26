package ai.t2x.virtualdevice.devd.actor
/*
import java.util.concurrent.atomic.AtomicInteger

import ai.t2x.lib.common.mqtt.{MqttWebSocketPublisher, MqttWebSocketSubscriber}
import ai.t2x.virtualdevice.devd.common.DevdConfig
import ai.t2x.virtualdevice.devd.mqtt.MqttMessageHandlerImpl
import akka.actor.{Actor, ActorRef}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable
import scala.collection.JavaConverters._
import ai.t2x.virtualdevice.devd.common.DevdJsonProtocol._
import spray.json._

import scala.concurrent.Promise

/**
  * 2018. 6. 21. - Created by Cho, Hee-Seung
  */
class RequestManagerActor extends Actor with StrictLogging {
  private val reqIdGenerator = new AtomicInteger()

  private val waitingBoard = mutable.HashMap[Long, ActorRef]()

  private val promiseBoard = mutable.HashMap[Long, Promise[Any]]()

  private val publisher: MqttWebSocketPublisher = MqttWebSocketPublisher(DevdConfig.config.getString("devd.mqtt.broker"), DevdConfig.config.getString("devd.mqtt.publisher.clientId"))

  private val subscriber: MqttWebSocketSubscriber = MqttWebSocketSubscriber(DevdConfig.config.getString("devd.mqtt.broker"), DevdConfig.config.getString("devd.mqtt.subscriber.clientId"))

  override def preStart(): Unit = {
    // subscribe topic to receive the reponse message
    logger.info("# RequestManagerActor preStart...")
    val topic: Array[String] = DevdConfig.config.getStringList("devd.mqtt.subscriber.topic").asScala.toArray
    topic.foreach(println(_))
    val qos: Array[Int] = topic.map(_ => 2)
    subscriber.sub(topic, qos, new MqttMessageHandlerImpl(self))
  }

  override def postStop(): Unit = {
  }

  override def receive: Receive = {
    case RequestMessage(topic, msg) =>
      logger.info("# Received RequestMessage message.")
      val reqId = reqIdGenerator.getAndIncrement()
      // TODO: need to distinguish among the nodes
      val replyTo = "requestors/"+reqId

      logger.info(s"===========================>\nto:${topic.toString}\nreplyTo: ${replyTo.toString}")
      // TODO: save current time along sender, so that we can expire waiting
      waitingBoard(reqId) = sender
//      smqd.publish(topic, ResponsibleMessage(replyTo, msg))
//      publisher.pub(topic, msg.toString.getBytes(), 2, null)
      val message: JsValue = ResponsibleMessage(replyTo, msg).toJson
      publisher.pub(topic, message.compactPrint.getBytes(), 2, null)
    case RequestMessageWithPromise(topic, msg, prms: Promise[Any]) =>
      logger.info("# Received RequestMessageWithPromise message.")
      val reqId = reqIdGenerator.getAndIncrement()
      // TODO: need to distinguish among the nodes
      val replyTo = "requestors/"+reqId

      logger.info(s"===========================>\nto:${topic.toString}\nreplyTo: ${replyTo.toString}")
      // TODO: save current time along sender, so that we can expire waiting
      promiseBoard(reqId) = prms
      //      smqd.publish(topic, ResponsibleMessage(replyTo, msg))
      //      publisher.pub(topic, msg.toString.getBytes(), 2, null)
      val message: JsValue = ResponsibleMessage(replyTo, msg).toJson
      publisher.pub(topic, message.compactPrint.getBytes(), 2, null)
    case OutboundPublish(topic, msg) =>
      logger.info(s"# Received OutboundPublish message. topic=${topic}, message=${msg.toString}")
      val reqIdStr = topic.split("/").last
      val reqId = reqIdStr.toLong

      promiseBoard.remove(reqId) match {
        case Some(prms: Promise[Any]) => prms.success(msg.asInstanceOf[String])
        case _ => logger.warn("Missing waiting board for reqId: {}", reqId)
      }
      /*
      waitingBoard.remove(reqId) match {
        case Some(origin) => origin ! msg
        case _ => logger.warn("Missing waiting board for reqId: {}", reqId)
      }
      */
  }
}

object RequestManagerActor {
  val actorName = "requestors"
}

case class RequestMessage(topic: String, msg: Any)
case class RequestMessageWithPromise[T](topic: String, msg: Any, prms: Promise[T])
case class ResponsibleMessage(replyTo: String, msg: Any)

case class OutboundPublish(topic: String, msg: Any)
*/