package ai.t2x.virtualdevice.devd.common

import com.typesafe.config.{Config, ConfigFactory}

/**
  * 2018. 6. 19. - Created by Cho, Hee-Seung
  */
object DevdConfig {
  lazy val config: Config = ConfigFactory.load()
  lazy val host: String = config.getString("devd.restapi.host")
  lazy val port: Int = config.getInt("devd.restapi.port")
}
