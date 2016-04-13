package com.service

import akka.http.scaladsl.server.Directives._
import com.json.JsonHelper
import com.db._
import com.routeHelpers.{RouteRequest, MessageData, SpaceData, EventData}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.{Failure, Success}

trait Routes extends JsonHelper {

  val logger = Logger(LoggerFactory.getLogger("routeLog"))

  val routes = {
    path("bk_scala" / "insert") {
      post {
        entity(as[String]) { json =>
          val event = parse(json).extract[EventData]
          val routeRequest = new RouteRequest(json, Database.insertEvent(event))

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
          val routeRequest = new RouteRequest(json, Database.getSpaceStats(space.spaceId))

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
          val routeRequest = new RouteRequest(json, Database.getMessageStats(message.messageId))

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
//curl --data "{\"spaceId\":5, \"messageId\": 5, \"eventType\": 5, \"nodeId\":1, \"forceException\": false}" http://127.0.0.1:9000/bk_scala/insert
//curl --data "{\"spaceId\":5, \"nodeId\":1, \"forceException\": false}" http://127.0.0.1:9000/bk_scala/spaceStatistics