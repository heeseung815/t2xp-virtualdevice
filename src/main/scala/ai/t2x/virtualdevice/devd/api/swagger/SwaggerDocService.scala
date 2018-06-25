package ai.t2x.virtualdevice.devd.api.swagger

import ai.t2x.virtualdevice.devd.api.controller.VirtualDeviceCtrl
import ai.t2x.virtualdevice.devd.common.DevdConfig
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.models.ExternalDocs
import io.swagger.models.auth.BasicAuthDefinition

/**
  * 2018. 6. 19. - Created by Cho, Hee-Seung
  */
object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses = Set(
    classOf[VirtualDeviceCtrl]
  )
  override val host = s"${DevdConfig.host}:${DevdConfig.port}"
  override val info = Info(version = "1.0")
  override val externalDocs = Some(new ExternalDocs("Core Docs", "http://acme.com/docs"))
  override val securitySchemeDefinitions = Map("basicAuth" -> new BasicAuthDefinition())
  override val unwantedDefinitions = Seq("Function1", "Function1RequestContextFutureRouteResult")
}
