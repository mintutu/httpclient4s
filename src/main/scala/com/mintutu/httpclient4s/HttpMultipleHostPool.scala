package com.mintutu.httpclient4s

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.mintutu.httpclient4s.retry.RetryLogic
import com.mintutu.httpclient4s.selector.Selector

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

abstract class HttpMultipleHostPool(servers: Seq[Uri],
                                    bufferSize: Int,
                                    retryStrategy: RetryLogic[HttpRequest, HttpResponse],
                                    overflowStrategy: OverflowStrategy)(
  implicit val system: ActorSystem,
  implicit val materializer: ActorMaterializer,
  implicit val executionContext: ExecutionContext) extends Selector[Uri] {

  override def hosts(): Seq[Uri] = servers

  val poolClientFlows = servers.map{
    host =>
      val flow = if (host.authority.port == 0){
        Http().cachedHostConnectionPool[Promise[HttpResponse]](host.authority.host.address())
      } else {
        Http().cachedHostConnectionPool[Promise[HttpResponse]](host.authority.host.address(), host.authority.port)
      }
      host -> flow
  }.toMap

  private lazy val queues = {
    val map = new ConcurrentHashMap[Uri, SourceQueueWithComplete[(HttpRequest, Promise[HttpResponse])]]
    poolClientFlows.map {
      pool =>
        val queue = Source.queue[(HttpRequest, Promise[HttpResponse])](bufferSize, overflowStrategy)
          .via(pool._2)
          .toMat(Sink.foreach({
            case ((Success(resp), p)) => p.success(resp)
            case ((Failure(e), p))    => p.failure(e)
          }))(Keep.left)
          .run()
        map.put(pool._1, queue)
    }
    map
  }

  private def queueRequest(request: HttpRequest): Future[HttpResponse] = {
    select() match {
      case None => Future.failed(NoAvailableServer("Don't find any available host"))
      case Some(uri) =>
        val responsePromise = Promise[HttpResponse]()
        queues.get(uri).offer(request -> responsePromise).flatMap {
          case QueueOfferResult.Enqueued    => responsePromise.future
          case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
          case QueueOfferResult.Failure(ex) => Future.failed(ex)
          case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
        }.map{
          resp =>
            success(uri)
            resp
        }.recoverWith{
          case t: Throwable =>
            failure(uri)
            println(t.getMessage, t)
            Future.failed(t)
        }
    }
  }

  def sendRequest(request: HttpRequest): Future[HttpResponse] = {
    retryStrategy.doRecover(request, queueRequest)
  }
}
