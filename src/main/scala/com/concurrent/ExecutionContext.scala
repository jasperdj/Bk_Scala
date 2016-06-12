package com.concurrent

import java.util.concurrent.Executors

/**
  * Created by a623557 on 30-5-2016.
  */
object ExecutionContext {
  ///TODO should be configurable
  val CONCURRENCY_FACTOR=5

  object IO{
    /***
      * Responsible to handle all DB calls
      */
    implicit lazy val dbOperations:concurrent.ExecutionContext = concurrent.ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()*CONCURRENCY_FACTOR)
    )
  }
}
