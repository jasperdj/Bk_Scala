package service

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import com.knoldus.json.JsonHelper
import com.knoldus.db.Database
import com.knoldus.db.Event
import akka.http.scaladsl.model.StatusCodes

import scala.util.{Failure, Success}

trait Routes extends JsonHelper {

  val routes =  {

    path("bk_scala" / "insert") {
      post {
        entity(as[String]) { json =>
          val event = parse(json).extract[Event]
          val insertRequest = Database.insertEvent(event.spaceId, event.messageId, event.eventType)

          insertRequest.onComplete({
            case Success(event) => complete{ HttpResponse(status = StatusCodes.Created) }
            case Failure(e) => complete { HttpResponse(status = StatusCodes.InternalServerError) }
          })

        }
      }
    }


  }
}


//curl --data "{\"totalExceptions\":0 ,\"id\":5 }" http://127.0.0.1:9000/bk_scala_up/update

