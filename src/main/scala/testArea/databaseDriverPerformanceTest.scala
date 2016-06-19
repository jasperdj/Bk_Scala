package testArea

import java.io.File
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.text.SimpleDateFormat
import java.util.Date

import com.db.Database
import com.routeHelpers.EventData
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by a623557 on 1-6-2016.
  */
object il2dl { implicit def intlist2dlist(il: List[Int]): List[Double] = il.map(_.toDouble) }

object databaseDriverPerformanceTest extends App {
  import il2dl._
  def time = System.currentTimeMillis()
  case object insert
  case object messageStats
  case object spaceStats

  //META INFORMATION
  val iterations = 3
  val subIterations = 4000
  val prototype = "BK"
  val techStack = "Scala"
  val startTimestamp = time

  val randomInt = new Random()

  def resetDatabase(): Unit = {
    val future = Database.dropCollection
    val result = Await.result(future, 30 seconds)
    println("Database is emptied (" + result.toString + ")")
    sleep
  }

  def parralelTest[T](sleep: Boolean, iterations: Int, dbRequest:T): List[Int] = {
    def iteration(i: Int, results: List[Int]): List[Int] = i match {
      case 0 => results
      case _ => iteration(i - 1, results :+ test)
    }

    def test:Int = {
      val t1 = time
      var futures = List[Future[Any]]()
      for (i <- 1 to subIterations) {
        val future = dbRequest match {
          case `insert` => Database.insertEvent(EventData(randomInt.nextInt(9), randomInt.nextInt(9), randomInt.nextInt(5)))
          case `messageStats` => Database.getMessageStats(randomInt.nextInt(9))
          case `spaceStats` => Database.getSpaceStats(randomInt.nextInt(9))
          case _ => throw new Error("Did not understand type.")
        }
        futures = futures :+ future
      }
      Await.result(Future.sequence(futures), 10 minutes)
      (time - t1).toInt
    }

    iteration(iterations, List())
  }

  def sequentialTest[T](sleep: Boolean, iterations: Int, dbRequest:T): List[Int] = {
    def iteration(i: Int, results: List[Int]): List[Int] = i match {
      case 0 => results
      case _ => iteration(i - 1, results :+ test)
    }

    def test: Int = {
      val t1 = time
      for (i <- 1 to subIterations) {
        Await.result(dbRequest match {
          case `insert` => Database.insertEvent(EventData(randomInt.nextInt(9), randomInt.nextInt(9), randomInt.nextInt(5)))
          case `messageStats` => Database.getMessageStats(randomInt.nextInt(9))
          case `spaceStats` => Database.getSpaceStats(randomInt.nextInt(9))
          case _ => throw new Error("Did not understand type.")
        }, 30 seconds)
      }
      Thread.sleep(500)
      (time - t1).toInt
    }

    iteration(iterations, List())
  }

  def parseTestResults[T](name: String, pause:Boolean, parallel:Boolean, queryType:T): Unit = {
    def getData = {
      if (parallel) {
        sequentialTest(pause, iterations, queryType)
      } else {
        parralelTest(pause, iterations, queryType)
      }
    }

    val stats = new Statistics(getData.toArray[Int])
    val dataRow = s"$startTimestamp,$iterations,$subIterations,$prototype,$techStack,$name,$pause,$parallel,${stats.min},${stats.mean},"+
      s"${stats.median()},${stats.max},${stats.stdError()}\n"
    Files.write(Paths.get("databaseBenchmark.txt"), dataRow.getBytes(), StandardOpenOption.APPEND);
  }

  def sleep: Unit = {
    //println("Sleeping 1000ms...")
    Thread.sleep(1000)
  }

  def benchmarkInsertQuery: Unit = {
    val t1 = time
    println("    Started insert benchmarkQuery.")
    resetDatabase()
    parseTestResults("insert",true,false,insert)
    resetDatabase()
    parseTestResults("insert", false, false, insert)
    resetDatabase()
    parseTestResults("insert", true, true, insert)
    resetDatabase()
    parseTestResults("insert", false, true, insert)
    println("    Finished insert benchmarkQuery in " + (time-t1) + " ms")
  }

  def benchmarkGetQuery[T](name:String, query:T): Unit = {
    val t1 = time
    println(s"$name started...")
    sleep
    parseTestResults(name, true, false, query)
    sleep
    parseTestResults(name, false, false, query)
    sleep
    parseTestResults(name, true, true, query)
    sleep
    parseTestResults(name, false, true, query)
    println(s"    Finished $name benchmarkQuery in ${time-t1} ms.")
  }

  def getDateTime():String = {
    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(new Date())
  }

  def benchmark(): Unit = {
    val t1 = time
    println(s"$getDateTime: benchmarking with $iterations iterations and $subIterations sub-iterations. ")
    benchmarkInsertQuery
    benchmarkGetQuery("spaceStats", spaceStats)
    benchmarkGetQuery("messageStats", messageStats)
    println(s"$getDateTime: benchmarking completed in ${time-t1} ms.")
  }

  benchmark
}
