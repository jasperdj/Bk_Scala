package testArea

import akka.actor.{ ActorSystem}
import akka.http.ServerSettings
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer}
import com.service.Routes



/**
  * Created by a623557 on 7-6-2016.
  */
object httpServicePerformanceTest extends App with Routes  {
  implicit val system:ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher= system.dispatcher
  val customSettings = ServerSettings(system).copy(maxConnections = 50000)
  Http().bindAndHandle(routes2, "0.0.0.0", 9000, customSettings)
  println("Server online")
}
