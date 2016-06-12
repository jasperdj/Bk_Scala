package testArea

import java.nio.file.{StandardOpenOption, Paths, Files}

import com.json.JsonHelper
import com.routeHelpers.EventData

import scala.concurrent.{Await, Future}
import scala.util.Random
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by a623557 on 7-6-2016.
  */
object serializationPerformanceTest extends JsonHelper with App {
  val random = new Random();

  val iterations = 20
  val subIterations = 20000
  val prototype = "BK"
  val techStack = "Scala"

  case object readTest
  case object writeTest
  case object mixTest

  benchmark

  def benchmark = {
    parseTestResult("read", true, readTest)
    parseTestResult("read", false, readTest)

    parseTestResult("write", true, writeTest)
    parseTestResult("write", false, writeTest)

    parseTestResult("mix", true, mixTest)
    parseTestResult("mix", false, mixTest)
    println("Done.")
  }

  def parseTestResult[T](name:String, parallel:Boolean, testType:T): Unit = {
    def getData:List[Int] = {
      if (parallel) parallelTest(testType)
      else sequentialTest(testType)
    }

    val stats = new Statistics(getData.toArray[Int])
    val dataRow = s"$iterations,$subIterations,$prototype,$techStack,$name,$parallel,${stats.min}," +
    s"${stats.mean},${stats.median},${stats.max},${stats.stdError}\n"
    Files.write(Paths.get("serializationBenchmark.txt"), dataRow.getBytes(), StandardOpenOption.APPEND);
  }

  // MEDIUM LEVEL BENCHMARKING /////////////////////////////////
  def parallelTest[T](testType:T):List[Int] = {
    def iterator(i:Int, result:List[Int]):List[Int] = i match {
      case 0 => result
      case _ => iterator(i-1, result :+ test)
    }

    def test:Int = {
      val t1 = time
      var futures:List[Future[Any]] = List()
      for(t<- 1 to subIterations) {
        val future = Future{ testOne(testType) }
        futures = futures :+ future
      }
      Await.result(Future.sequence(futures), 10 minutes)
      (time-t1).toInt
    }

    iterator(iterations, List())
  }

  def sequentialTest[T](testType:T):List[Int] = {
    def iterator(i:Int, result:List[Int]):List[Int] = i match {
      case 0 => result
      case _ => iterator(i-1, result :+ test)
    }

    def test:Int = {
      val t1 = time
      for (t <- 0 until subIterations) testOne(testType)
      (time - t1).toInt
    }

    iterator(iterations, List())
  }

  // LOW LEVEL BENCHMARKING ////////////////////////////////////////////////
  def testOne[T](testType:T): Unit = {
    testType match {
      case `readTest` => read(getJson)
      case `writeTest` => write(getEventData)
      case `mixTest` => mix(getEventData)
    }
  }

  def read(json:String):EventData = {
    parse(json).extract[EventData];
  }

  def mix(eventData: EventData): EventData = {
    read(write(eventData))
  }

  def getEventData:EventData = {
    new EventData(random.nextInt(9), random.nextInt(9), random.nextInt(5))
  }

  def getJson:String = {
    "{ \"spaceId\": "+ random.nextInt(9)+", \"messageId\": "+ random.nextInt(9)+", \"eventType\": "+random.nextInt(5)+" }"
  }

  //Helper functions ///////////////////////////////
  def time = System.currentTimeMillis()

}
