package com.service

import akka.actor.{ ActorSystem}
import akka.http.ServerSettings
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer}

import com.routeHelpers.Monitor


import scala.runtime.BoxedUnit


object HttpService extends App with Routes    {
  implicit val system:ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher= system.dispatcher

  val customSettings = ServerSettings(system).copy(maxConnections = 50000)
  Http().bindAndHandle(routes, "0.0.0.0", 9000, customSettings)

  println("Server up and running...")

  Monitor
}