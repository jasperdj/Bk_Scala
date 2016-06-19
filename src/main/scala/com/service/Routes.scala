package com.service

import akka.http.scaladsl.model.{StatusCodes, HttpResponse}
import akka.http.scaladsl.server.Directives._
import com.json.JsonHelper
import com.db._
import com.routeHelpers.{RouteRequest, MessageData, SpaceData, EventData}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.{Random, Failure, Success}

//DELETE THIS
import scala.concurrent.ExecutionContext.Implicits.global

trait Routes extends JsonHelper {

  val logger = Logger(LoggerFactory.getLogger("routeLog"))

  val random = new Random
  val iterations = 100000
  val routes2 = {
    path("NOTHING") {
      get {
          complete {
            HttpResponse(StatusCodes.OK, entity = "Succes")
          }
      }
    } ~
    path("CPU_async") {
      get {
        val future = Future {
          for (i <- 0 to iterations * 2) {
            Math.sqrt(random.nextDouble * i)
            Math.abs(random.nextDouble / i)
            Math.tan(random.nextDouble - 1)
          }
        }
        onComplete(future) {
          case Success(results) => complete {
            HttpResponse(StatusCodes.OK, entity = "Succes")
          }
          case Failure(error) => complete {
            HttpResponse(StatusCodes.InternalServerError, entity = "failure")
          }
        }
      }
    }~
    path("CPU") {
      get {
        complete {
          for (i <- 0 to iterations * 2) {
            Math.sqrt(random.nextDouble * i)
            Math.abs(random.nextDouble / i)
            Math.tan(random.nextDouble - 1)
          }

          HttpResponse(StatusCodes.OK, entity = "Succes")
        }
      }
    } ~
    path("MEMORY_async") {
      get {
        val future = Future {
          var list = ListBuffer[String]()
          var list2 = ListBuffer[ListBuffer[String]]()
          for (i <- 0 to iterations) {
            list += "qwertytuyiopsadfghjklzxcvbnm"
          }
          for (i <- 0 to iterations) {
            list2 += list
          }
        }

        onComplete(future) {
          case Success(results) => complete {
            HttpResponse(StatusCodes.OK, entity = "Succes")
          }
          case Failure(error) => complete {
            HttpResponse(StatusCodes.InternalServerError, entity = "failure")
          }
        }
      }
    } ~
    path("MEMORY") {
      get {
        complete {
          var list = ListBuffer[String]()
          var list2 = ListBuffer[ListBuffer[String]]()
          for (i <- 0 to iterations) {
            list += "qwertytuyiopsadfghjklzxcvbnm"
          }
          for (i <- 0 to iterations) {
            list2 += list
          }

          HttpResponse(StatusCodes.OK, entity = "Succes")
        }
      }
    }
  }



  val routes = {
    path("bk_scala" / "insert") {
      post {
        entity(as[String]) { json =>
          println(json)
          val event = parse(json).extract[EventData]
          val routeRequest = new RouteRequest(json, Database.insertEvent(event), "Insert")

          onComplete(routeRequest.waitingOperations) {
            case Success(results) => complete {
              routeRequest.success(results)
            }
            case Failure(error) => complete {
              routeRequest.failure(error)
            }
          }
        }
      }
    } ~
    path("bk_scala" / "spaceStatistics") {
      post {
        entity(as[String]) { json =>
          val space = parse(json).extract[SpaceData]
          val routeRequest = new RouteRequest(json, Database.getSpaceStats(space.spaceId), "spaceStatistics")

          onComplete(routeRequest.waitingOperations) {
            case Success(results) => complete {
              routeRequest.success(results)
            }
            case Failure(error) => complete {
              routeRequest.failure(error)
            }
          }
        }
      }
    } ~
    path("bk_scala" / "messageStatistics") {
      post {
        entity(as[String]) { json =>
          val message = parse(json).extract[MessageData]
          val routeRequest = new RouteRequest(json, Database.getMessageStats(message.messageId), "messageStatistics")

          onComplete(routeRequest.waitingOperations) {
            case Success(results) => complete {
              routeRequest.success(results)
            }
            case Failure(error) => complete {
              routeRequest.failure(error)
            }
          }
        }
      }
    }
  }
}

//curl --data "{\"totalExceptions\":0 ,\"id\":5 }" http://127.0.0.1:9000/bk_scala_up/update
//curl --data "{\"spaceId\":5, \"messageId\": 5, \"eventType\": 5, \"nodeId\":1, \"forceException\": false}" http://raspberrypi.mshome.net:9000/bk_scala/insert
//curl --data "{\"spaceId\":5, \"nodeId\":1, \"forceException\": false}" http://127.0.0.1:9000/bk_scala/spaceStatistics