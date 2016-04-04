import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import com.knoldus.db.Database



object test extends App {
  val db = Database
  Thread.sleep(1000)


  val spaceStats = db.getSpaceStats(1)

  spaceStats.onComplete({
    case Success(list) => {
      println(list)
    }
    case Failure(e) => {
      println(s"Error: $e")
    }
  })
  println(spaceStats)

















  /*println("test")
  val future1 = Future {
    1*2
  }

  val future2 = Future {
    2*2
  }

  val list = for {
    f1 <- future1
    f2 <- future2
  }yield(f1, f2)

  list.onComplete({
    case Success(list2) => {
      println(list2._1)
    }
    case Failure(e) => {
      println(s"Error $e")
    }
  })

  Thread.sleep(1000)*/





}
