package com.mintutu.httpclient4s.example

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.mintutu.httpclient4s.MultipleHostPool
import com.mintutu.httpclient4s.retry.InfiniteStrategy
import com.mintutu.httpclient4s.selector.WeightedRoundRobinSelector

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

object MultipleHostPoolSpec extends App {

  implicit val system = ActorSystem()
  import system.dispatcher // to get an implicit ExecutionContext into scope
  implicit val materializer = ActorMaterializer()

  val servers1 = Seq(Uri(s"http://akka1.io"), Uri(s"http://google.com"))
  val httpClient = new MultipleHostPool(
    servers1,
    bufferSize = 10,
    new InfiniteStrategy(),
    OverflowStrategy.dropBuffer
  ) with WeightedRoundRobinSelector {
    override val defaultWeight: Int = 100
    override val decrementBy: Int = 30
  }

  (1 to 100).foreach {
    e => Try {
      val response = httpClient.sendRequest(HttpRequest(uri = "/"))
      val result = Await.result(response, 10 seconds)
      println(s"Get Response $e: $result")
    }
  }

}
