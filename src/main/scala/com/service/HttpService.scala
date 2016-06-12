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



  //Http().bindAndHandleSync(requestHandler, "0.0.0.0", 8080)

  /*val flowBufferingElements = Flow[HttpRequest].buffer(1000, OverflowStrategy.backpressure) // back-pressures the source if the buffer is full

  //Http().bind("0.0.0.0", 9000).via(flowBufferingElements).toMat(requestHandler)

 val requestHandler: HttpRequest => HttpResponse = {
   case HttpRequest(GET, Uri.Path("/test"), _, _, _) => HttpResponse(200, entity = "It works!")

 }*/


  println("Server up and running...")

  Monitor
}