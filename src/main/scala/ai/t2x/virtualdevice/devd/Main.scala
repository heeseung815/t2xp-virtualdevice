package ai.t2x.virtualdevice.devd

import akka.actor.ActorSystem
import com.typesafe.scalalogging.StrictLogging

/**
  * 2018. 6. 19. - Created by Cho, Hee-Seung
  */
object Main extends App with StrictLogging {
  implicit val actorSystem: ActorSystem = ActorSystem("DEVD")

  val restApi = RestApi()
  restApi.start

  logger.info("DEVD started!")
}
