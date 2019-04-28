package com.mintutu.httpclient4s

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.mintutu.httpclient4s.retry.InfiniteStrategy
import com.mintutu.httpclient4s.selector.RoundRobinSelector

import scala.concurrent.Await
import scala.concurrent.duration._

object MultipleHostPoolSpec extends App {

  implicit val system = ActorSystem()
  import system.dispatcher // to get an implicit ExecutionContext into scope
  implicit val materializer = ActorMaterializer()

  val servers1 = Seq(Uri(s"http://akka.io"), Uri(s"http://google.com"))
  val x = new MultipleHostPool(
    servers1,
    bufferSize = 10,
    new InfiniteStrategy(),
    OverflowStrategy.dropBuffer
  ) with RoundRobinSelector

  val response1 = x.sendRequest(HttpRequest(uri = "/"))
  val result1 = Await.result(response1, 10 seconds)
  println(result1)
}
