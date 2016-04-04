package com.knoldus.db

import reactivemongo.api._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.LastError
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Success, Failure}

// BSON implementation of the count command
import reactivemongo.api.commands.bson.BSONCountCommand.{ Count, CountResult }

import reactivemongo.api.commands.bson.BSONCountCommandImplicits._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.iteratee.Iteratee

import reactivemongo.bson.BSONDocument
import reactivemongo.api.collections.bson.BSONCollection

object Database {

  val collection = connect()

  def connect(): BSONCollection = {

    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))

    val db = connection("Bk_Scala")
    db.collection("Events")
  }

  def insertEvent(spaceId:Int, messageId:Int, eventType:Int) : Future[WriteResult] = {
    val query = BSONDocument("spaceId" -> spaceId, "messageId" -> messageId, "eventType" -> eventType)

    Database.collection
      .insert(query)
  }

  /*
    @return messages created, messages deleted.
   */
  def getSpaceStats(spaceId:Int) : Future[(CountResult, CountResult)] = {

    val getCreatedQuery = BSONDocument("spaceId" -> spaceId, "eventType" -> 1)
    val getDeletedQuery = BSONDocument("spaceId" -> spaceId, "eventType" -> 2)

    val commandCreated = Count(getCreatedQuery)
    val resultCreated: Future[CountResult] = collection.runCommand(commandCreated)

    val commandDeleted = Count(getDeletedQuery)
    val resultDeleted: Future[CountResult] = collection.runCommand(commandDeleted)

    val fields = for{
      f1Result <- resultCreated
      f2Result <- resultDeleted
    } yield (f1Result, f2Result)

    fields
  }

  def getMessageStats(messageId:Int) : Future[(CountResult, CountResult)] = {
    val getLikeQuery = BSONDocument("messageId" -> messageId, "eventType" -> 4)
    val getUnlikeQuery = BSONDocument("messageId" -> messageId, "eventType" -> 5)

    val commandLikes = Count(getLikeQuery)
    val resultLikes: Future[CountResult] = collection.runCommand(commandLikes)

    val commandUnlikes = Count(getUnlikeQuery)
    val resultUnlikes: Future[CountResult] = collection.runCommand(commandUnlikes)

    val fields = for{
      f1Result <- resultLikes
      f2Result <- resultUnlikes
    } yield (f1Result, f2Result)

    fields
  }

  /*
  def findAllTickers(): Future[List[BSONDocument]] = {
    val query = BSONDocument()
    val filter = BSONDocument("Company" -> 1, "Country" -> 1, "Ticker" -> 1)

    // which results in a Future[List[BSONDocument]]
    Database.collection
      .find(query, filter)
      .cursor[BSONDocument]
      .collect[List]()
  }

  def findTicker(ticker: String) : Future[Option[BSONDocument]] = {
    val query = BSONDocument("Ticker" -> ticker)

    Database.collection
      .find(query)
      .one
  }*/

}