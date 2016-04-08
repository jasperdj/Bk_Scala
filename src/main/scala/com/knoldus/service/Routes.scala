package com.knoldus.service

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import com.knoldus.json.JsonHelper
import com.knoldus.db.Database
import com.knoldus.db.Event
import akka.http.scaladsl.model.StatusCodes

import scala.util.{Failure, Success}

trait Routes extends JsonHelper {

  val routes = {

    path("test") {
      get {
        complete {
          HttpResponse(entity = "test")
        }
      }
    }
    path("bk_scala" / "insert") {
      post {
        entity(as[String]) { json =>
          val event = parse(json).extract[Event]
          val insertRequest = Database.insertEvent(event.spaceId, event.messageId, event.eventType)
          onComplete(insertRequest) {
            case Success(response) => complete {
              HttpResponse(status = StatusCodes.Created, entity = write(StatusCodes.Created.defaultMessage))
            }
            case Failure(e) => complete {
              HttpResponse(StatusCodes.InternalServerError, entity = write(StatusCodes.InternalServerError.defaultMessage))
            }
          }
        }
      }
    } ~
    path("bk_scala" / "spaceStatistics" / IntNumber) { id =>
      get {
        val querySpaceStatistics = Database.getSpaceStats(id)
        onComplete(querySpaceStatistics) {
          case Success(statistics) => complete {
            HttpResponse(StatusCodes.OK, entity = write(statistics))
          }
          case Failure(e) => complete {
            HttpResponse(StatusCodes.InternalServerError, entity = write(StatusCodes.InternalServerError.defaultMessage))
          }
        }
      }
    } ~
      path("bk_scala" / "messageStatistics" / IntNumber) { id =>
        get {
          val queryMessageStatistics = Database.getMessageStats(id)
          onComplete(queryMessageStatistics) {
            case Success(statistics) => complete {
              HttpResponse(StatusCodes.OK, entity = write(statistics))
            }
            case Failure(e) => complete {
              HttpResponse(StatusCodes.InternalServerError, entity = write(StatusCodes.InternalServerError.defaultMessage))
            }
          }
        }
      }


  }
}


//curl --data "{\"totalExceptions\":0 ,\"id\":5 }" http://127.0.0.1:9000/bk_scala_up/update
//curl --data "{\"spaceId\":5, \"messageId\": 5, \"eventType\": 5}" http://127.0.0.1:9000/bk_scala/insert

