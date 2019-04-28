package com.mintutu.httpclient4s.selector

import akka.http.scaladsl.model.Uri
import org.scalatest.{FlatSpec, Matchers}

class WeightedRoundRobinSelectorSpec extends FlatSpec with Matchers with PercentageTestHelper{

  val TEST_SIZE = 5000
  val uri1 = Uri("http://192.168.0.1")
  val uri2 = Uri("http://192.168.0.2")
  val uri3 = Uri("http://192.168.0.3")

  "WeightedRoundRobinSelector.next" should "return evenly distributed items" in {
    val resource = new WeightedRoundRobinSelector() {
      override def hosts = Seq(uri1, uri2, uri3)
      override val decrementBy: Int = 1
      override val defaultWeight: Int = 10
    }
    val result = (1 to TEST_SIZE).map(_ => resource.select())
    val groupedResult = result.flatten.toList.groupBy(identity).mapValues(e => e.size * 100.0 / TEST_SIZE)
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri1, 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri2, 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri3, 0)) shouldBe true
  }

  "WeightedRoundRobinSelector.next" should "return items based on weight" in {
    val resource = new WeightedRoundRobinSelector() {
      override def hosts = Seq(uri1, uri2, uri3)
      override val decrementBy: Int = 5
      override val defaultWeight: Int = 20
    }
    resource.failure(uri1)
    resource.failure(uri1) //10
    resource.failure(uri2) //15
    val result = (1 to TEST_SIZE).map(_ => resource.select)
    val groupedResult = result.flatten.toList.groupBy(identity).mapValues(e => e.size * 100.0 / TEST_SIZE)
    isUnderPercentAllocation(22, groupedResult.getOrElse(uri1, 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri2, 0)) shouldBe true
    isUnderPercentAllocation(45, groupedResult.getOrElse(uri3, 0)) shouldBe true
  }

  "WeightedRoundRobinSelector.failure" should "remove item if weight <= 0" in {
    val resource = new WeightedRoundRobinSelector() {
      override def hosts = Seq(uri1, uri2, uri3)
      override val decrementBy: Int = 10
      override val defaultWeight: Int = 20
    }
    resource.failure(uri1)
    resource.failure(uri1) //0
    val result = (1 to TEST_SIZE).map(_ => resource.select)
    val groupedResult = result.flatten.toList.groupBy(identity).mapValues(e => e.size * 100.0 / TEST_SIZE)
    isUnderPercentAllocation(0, groupedResult.getOrElse(uri1, 0)) shouldBe true
    isUnderPercentAllocation(50, groupedResult.getOrElse(uri2, 0)) shouldBe true
    isUnderPercentAllocation(50, groupedResult.getOrElse(uri3, 0)) shouldBe true
  }

  "WeightedRoundRobinSelector.success" should "set default value to weight item" in {
    val resource = new WeightedRoundRobinSelector() {
      override def hosts = Seq(uri1, uri2, uri3)
      override val decrementBy: Int = 10
      override val defaultWeight: Int = 20
    }
    resource.failure(uri1)
    resource.success(uri1) //0
    val result = (1 to TEST_SIZE).map(_ => resource.select)
    val groupedResult = result.flatten.toList.groupBy(identity).mapValues(e => e.size * 100.0 / TEST_SIZE)
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri1, 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri2, 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri3, 0)) shouldBe true
  }
}
