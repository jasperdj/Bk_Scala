package com.routeHelpers

/**
  * Created by a623557 on 4-4-2016.
  */

case class EventData(spaceId: Int, messageId: Int, eventType:Int)
case class SpaceData(spaceId:Int)
case class MessageData(messageId:Int)

case class BenchmarkInput(nodeId: Int, forceException: Boolean)
case class BenchmarkOutput(resourceUtil:Map[String, Double], unitPerformance:Map[String, Long])
case class OutputData[T](output:T, benchmarkInput:BenchmarkInput, benchmarkOutput:BenchmarkOutput)