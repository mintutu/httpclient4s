package com.mintutu.httpclient4s.retry

import scala.concurrent.{ExecutionContext, Future}

class DropStrategy[IN, OUT](success: IN => OUT)(implicit ec: ExecutionContext) extends RetryLogic[IN, OUT] {
  override def doRecover(input: IN, future: IN => Future[OUT]): Future[OUT] = future(input).recover {
    case _ => success(input)
  }
}