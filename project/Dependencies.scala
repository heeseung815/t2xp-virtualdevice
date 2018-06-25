/**
  * 2018. 4. 23. - Created by Cho, Hee-Seung
  */

import sbt._

object Dependencies {

  object Versions {
    val scala = "2.12.5"
    val akka = "2.5.11"
    val akka_http = "10.1.1"
  }

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka force(),
    "com.typesafe.akka" %% "akka-remote" % Versions.akka force(),
    "com.typesafe.akka" %% "akka-http" % Versions.akka_http,
    "com.typesafe.akka" %% "akka-http-spray-json" % Versions.akka_http
  )

  val swagger = Seq(
    "io.swagger" % "swagger-jaxrs" % "1.5.18",
    "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.14.0"
  )

  val cors = Seq(
    "ch.megard" %% "akka-http-cors" % "0.3.0"
  )

//  val slick = Seq(
//    "com.typesafe.slick" %% "slick" % "3.2.3",
//    "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3"
//  )

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3" ,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  )

//  val sqlite = Seq(
//    "org.xerial" % "sqlite-jdbc" % "3.21.0"
//  )

//  val mariadb = Seq(
//    "org.mariadb.jdbc" % "mariadb-java-client" % "2.2.1"
//  )

//  val postgresql = Seq(
//    "org.postgresql" % "postgresql" % "42.2.0"
//  )

//  val phantom = Seq(
//    "com.outworkers" %% "phantom-dsl" % "2.20.0"
//  )

//  val fdb = Seq(
//    "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0"
//  )

  val mqtt = Seq(
    "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.2.0"
  )

  val uangel = Seq(
//    "ai.t2x" % "lib-verinfo_2.12" % "1.0.0-SNAPSHOT",
    "ai.t2x" % "lib-common_2.12" % "1.0.0-SNAPSHOT"
  )
}
