package com.mintutu.httpclient4s.retry

import scala.concurrent.Future

trait RetryLogic[IN, OUT] {
  def doRecover(input: IN, future: IN => Future[OUT]): Future[OUT]
}