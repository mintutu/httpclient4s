package com.mintutu.httpclient4s.retry

import scala.concurrent.{ExecutionContext, Future}

class InfiniteStrategy[IN, OUT](implicit ec: ExecutionContext) extends RetryLogic[IN, OUT] {
  override def doRecover(input: IN, future: IN => Future[OUT]): Future[OUT] = {
    future(input).recoverWith {
      case _ => doRecover(input, future)
    }
  }
}