package com.routeHelpers

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.sys.process._
import scala.util.{Failure, Success}

/**
  * Created by a623557 on 8-4-2016.
  */

case class update()
case class currentLoad()

object Monitor {
  val system = ActorSystem("monitorSystem")
  val resourceMonitor = system.actorOf(Props(new CPU_Monitor))
  implicit val timeout = Timeout(25 seconds)

  val isUnix = System.getProperty("os.name") != "Windows 7"
  if (isUnix)
  system.scheduler.schedule(0 seconds, 5 seconds, resourceMonitor, update)
}


class CPU_Monitor extends Actor {
  var latestCpuLoad = -1.0
  var latestRamUsed = -1.0

  override def receive: Receive = {
    case `update` => updateCPU
    case `currentLoad` => sender ! Map("cpuLoad" -> latestCpuLoad, "ramUsed" -> latestRamUsed)
    case _ => println("Error: Did not expect that!")
  }

  def updateCPU: Unit = {
    val gettingCpuStatistics:Future[String] = Future {
      "top -bn2 "!!
    }

    gettingCpuStatistics.onComplete {
      case Success(cpuResult) => processCPUResult(cpuResult)
      case Failure(t) => println("An error has occured: " + t.getMessage)
    }
  }

  def processCPUResult(resourceResult:String): Unit = {
    val cpuExpression = "%Cpu\\(s\\): +(\\d+\\.\\d+) us, +(\\d+\\.\\d+)".r
    val latestUserCpuLoad = cpuExpression.findAllMatchIn(resourceResult).map(_.group(1)).toList(1).toDouble
    val latestSystemCpuLoad = cpuExpression.findAllMatchIn(resourceResult).map(_.group(2)).toList(1).toDouble
    latestCpuLoad = latestUserCpuLoad + latestSystemCpuLoad

    val ramExpression = "KiB Mem: +(\\d+) total, +(\\d+)".r
    latestRamUsed = ramExpression.findAllMatchIn(resourceResult).map(_.group(2)).toList.head.toDouble
  }
}