import com.db.Database
import com.routeHelpers.EventData
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span, Millis}

import reactivemongo.api.commands.bson.BSONCountCommand.{Count, CountResult}
import reactivemongo.api.commands.bson.BSONCountCommandImplicits._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import reactivemongo.api.collections.bson.BSONCollection

import reactivemongo.bson.BSONDocument

/**
  * Created by a623557 on 9-5-2016.
  */

class databaseTest extends FunSuite with Matchers with ScalaFutures {

  test("The database driver should have a collection") {
      Database.collection shouldBe a [BSONCollection]
  }

  test("Database should be empty after dropping the collection") {
    Await.result(Database.dropCollection, 2 seconds)
    Database.collection.create()
    val stats = Database.collection.stats
    val result = Await.result(stats, 2 seconds)
    result.count shouldBe 0
  }

  test("Inserting events into database should return true") {
    val addingEvent = Database.insertEvent(EventData(2, 2, 1))
    val addingEvent_result = Await.result(addingEvent, 2 seconds)
    addingEvent_result.ok shouldBe true

    val addingAnotherEvent = Database.insertEvent(EventData(3, 3, 5))
    whenReady(addingAnotherEvent, timeout(2 seconds)) { result =>
      result.ok shouldBe true
    }
  }

  test("Space stats should return 1") {
    val gettingSpaceStats = Database.getSpaceStats(2)
    whenReady(gettingSpaceStats, timeout(2 seconds)) { result =>
      result shouldBe 1
    }
  }

  test("Message stats should return -1") {
    val gettingMessageStats = Database.getMessageStats(3)
    whenReady(gettingMessageStats, timeout(2 seconds)) { result =>
      result shouldBe -1
    }
  }
}