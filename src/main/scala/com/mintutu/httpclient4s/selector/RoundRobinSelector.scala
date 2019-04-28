package com.mintutu.httpclient4s.selector

import java.util.concurrent.atomic.AtomicInteger

import akka.http.scaladsl.model.Uri

trait RoundRobinSelector extends Selector[Uri]{

  lazy val servers: Array[Uri] = hosts.toArray

  private val poolIndex: AtomicInteger = new AtomicInteger()

  override def select(): Option[Uri] = {
    if (servers.size > 0) {
      Some(servers(poolIndex.incrementAndGet() % hosts.size))
    } else {
      None
    }
  }

  override def failure(t: Uri): Unit = {}

  override def success(t: Uri): Unit = {}
}
