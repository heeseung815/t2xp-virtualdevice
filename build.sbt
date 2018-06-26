import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

organization := "ai.t2x"

name := "virtualdevice"

version := "1.0.0-SNAPSHOT"

scalaVersion := Dependencies.Versions.scala

scalacOptions ++= Seq("-feature" /*, "-deprecation" */)

// resolving libraries stored on nexus
//resolvers ++= Seq(
//  "Nexus: maven-snapshots" at "http://192.168.7.150:8081/repository/maven-snapshots",
//  "Nexus: maven-releases" at "http://192.168.7.150:8081/repository/maven-releases"
//)

//excludesourcecodeswhenpublish
publishArtifact in (Compile, packageSrc) := false

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

//val gitCommitFull = "git rev-parse HEAD".!!.trim
//val gitCommitFile = s"echo $gitCommitFull" #> file("src/main/resources/VERSION.NFO") !

lazy val virtualdevice = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "virtualdevice",
    libraryDependencies ++= Dependencies.logging
      ++ Dependencies.akka
      ++ Dependencies.swagger
      ++ Dependencies.cors
//      ++ Dependencies.slick
//      ++ Dependencies.sqlite
//      ++ Dependencies.mariadb
//      ++ Dependencies.postgresql
//      ++ Dependencies.phantom
//      ++ Dependencies.fdb
      ++ Dependencies.mqtt
//      ++ Dependencies.uangel
  )
  // publish settings
  .settings(
  publish := (),  // no publish
  publishLocal := (),
  publishArtifact := false
)
  // native-packager settings
  .settings(
  mappings in Universal ++= directory("bin"),
  mappings in Universal ++= directory("conf").filter{ case (file, _) =>
    file.getName match {
      case _ => false
    }
  },
  packageName in Universal := s"virtualdevice-${(version in ThisBuild).value}",
  executableScriptName := "virtualdevice",
  bashScriptConfigLocation := Some("${VIRTUALDEVICE_HOME_DIR}/conf/process.ini")
)
  // sbt runtime settings
  .settings(
  mainClass in Compile := Some("ai.t2x.virtualdevice.devd.Main"),
  javaOptions in run ++= Seq(
    "-Dconfig.file=./conf/application.conf"
  ),
  fork in run := true
  )
  // docker settings
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    mappings in Docker += file("conf/application.conf") -> "/opt/virtualdevice/conf/application.conf",
    mappings in Docker += file("conf/process.ini") -> "/opt/virtualdevice/conf/process.ini",
    packageName in Docker := "virtualdevice",
    dockerBaseImage := "openjdk:jre-alpine",
    defaultLinuxInstallLocation in Docker := "/opt/virtualdevice",
    bashScriptConfigLocation := Some("/opt/virtualdevice/conf/process.ini"),
    dockerEntrypoint := Seq("/opt/virtualdevice/bin/virtualdevice"),
    dockerRepository := Some("192.168.7.150:8083"),
    dockerExposedPorts := Seq(9004)
  )