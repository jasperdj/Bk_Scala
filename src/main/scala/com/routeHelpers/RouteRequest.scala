package com.routeHelpers

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.util.Timeout
import com.json.JsonHelper
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by a623557 on 13-4-2016.
  */
class RouteRequest[T](json:String, databaseRequest:Future[T], unitName:String) extends JsonHelper{

  implicit val timeout = Timeout(25 seconds)

  //Convert JSON to case classes.
  val t1 = time
  val benchmarkInput = parse(json).extract[BenchmarkInput]

  if (benchmarkInput.forceException) throw new Error("Forced Exception")

  //Create futures for getting data from db and monitor
  val t2 = time
  val retrieveBenchmarkOutput = Monitor.resourceMonitor ? currentLoad
  val insertRequest = databaseRequest
  val waitingOperations = Future.sequence(List(retrieveBenchmarkOutput, insertRequest))
  val t3 = time

  def success(results:List[Any]):HttpResponse = {
    HttpResponse(StatusCodes.OK, entity = write(
      OutputData(results(1), benchmarkInput, BenchmarkOutput(results.head.asInstanceOf[Map[String, Double]],
        createUnitPerformance(List(t1, t2, t3, time))
      ))))
  }

  def failure(error:Throwable):HttpResponse = {
    println("Error: " + error)
    HttpResponse(StatusCodes.InternalServerError, entity = write(OutputData(error, benchmarkInput, BenchmarkOutput(null,
      createUnitPerformance(List(t1, t2, t3, time))
    ))))
  }

  private def time:Long = System.currentTimeMillis()

  private def createUnitPerformance(list:List[Long]):Map[String, Long] = {
    def iterator(list:List[Long], map:Map[String, Long]):Map[String, Long] =
    {
      list match {
        case x :: Nil => map
        case x :: tail => iterator(list.drop(1), map.updated(unitName +"_Unit"+map.size,  tail.head - x))
      }
    }
    iterator(list, Map())
  }
}
