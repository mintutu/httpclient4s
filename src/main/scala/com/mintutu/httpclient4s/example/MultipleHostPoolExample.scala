package com.mintutu.httpclient4s.example

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.mintutu.httpclient4s.HttpMultipleHostPool
import com.mintutu.httpclient4s.retry.InfiniteStrategy
import com.mintutu.httpclient4s.selector.WeightedRoundRobinSelector

import scala.concurrent.Await
import scala.concurrent.duration._

object MultipleHostPoolExample extends App {

  implicit val system = ActorSystem()
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val servers = Seq(Uri(s"http://192.168.0.100:8080"), Uri(s"http://192.168.0.101:8080"))
  val httpClient = new HttpMultipleHostPool(
    servers,
    bufferSize = 10,
    new InfiniteStrategy(),
    OverflowStrategy.dropBuffer
  ) with WeightedRoundRobinSelector {
    override val defaultWeight: Int = 100
    override val decrementBy: Int = 30
  }

  val response = httpClient.sendRequest(HttpRequest(uri = "/query"))
  val result = Await.result(response, 10 seconds)
  println(s"Get Response: $result")

}
