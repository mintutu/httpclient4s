# HTTP Client for Scala
[![Build Status](https://travis-ci.com/mintutu/httpclient4s.svg?branch=master)](https://travis-ci.com/mintutu/httpclient4s)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/cac234a768dd4706ae7bb3a742ecc394)](https://www.codacy.com/app/specterbn/httpclient4s?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mintutu/httpclient4s&amp;utm_campaign=Badge_Grade)
## Introduction
Build on top Akka HTTP Client to support multiple hosts and different selector strategies and retry strategies

## Making a  Request
```scala
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

```