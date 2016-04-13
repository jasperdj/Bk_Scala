package com.service

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.routeHelpers.Monitor
import scala.concurrent.Future
import scala.util.{Success, Failure}
import sys.process._


object HttpService extends App with Routes    {
  implicit val system:ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher= system.dispatcher

  Http().bindAndHandle(routes, "0.0.0.0", 9000)

  println("Server up and running...")

  val monitor = Monitor
}