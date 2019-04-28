package com.mintutu.httpclient4s.selector

import akka.http.scaladsl.model.Uri

trait WeightedRoundRobinSelector extends Selector[Uri]{

  lazy val servers: List[Uri] = hosts.toList
  val defaultWeight: Int
  val decrementBy: Int

  private lazy val weightedRoundRobin = new WeightedRoundRobinResource[Uri](servers, defaultWeight, decrementBy)

  override def select(): Option[Uri] = weightedRoundRobin.next

  override def success(t: Uri): Unit = weightedRoundRobin.reset(t)

  override def failure(t: Uri): Unit = weightedRoundRobin.decrement(t)
}
