package com.mintutu.httpclient4s.selector

import org.scalatest.{FlatSpec, Matchers}

class WeightedRoundRobinResourceSpec extends FlatSpec with Matchers {

  val TEST_SIZE = 5000

  "WeightedRoundRobinResource.next" should "return evenly distributed items" in {
    val items = List("a", "b", "c")
    val resource = new WeightedRoundRobinResource[String](items, decrementBy = 1, defaultWeight = 10)
    val result = (1 to TEST_SIZE).map(_ => resource.next)
    val groupedResult = result.flatten.toList.groupBy(identity).mapValues(e => e.size * 100.0 / TEST_SIZE)
    isUnderPercentAllocation(33, groupedResult.getOrElse("a", 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse("b", 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse("c", 0)) shouldBe true
  }

  "WeightedRoundRobinResource.next" should "return items based on weight" in {
    val items = List("a", "b", "c")
    val resource = new WeightedRoundRobinResource[String](items, decrementBy = 5, defaultWeight = 20)
    resource.decrement("a")
    resource.decrement("a") //10
    resource.decrement("b") //15
    val result = (1 to TEST_SIZE).map(_ => resource.next)
    val groupedResult = result.flatten.toList.groupBy(identity).mapValues(e => e.size * 100.0 / TEST_SIZE)
    isUnderPercentAllocation(22, groupedResult.getOrElse("a", 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse("b", 0)) shouldBe true
    isUnderPercentAllocation(45, groupedResult.getOrElse("c", 0)) shouldBe true
  }


  def isUnderPercentAllocation(expectation: Double, result: Double): Boolean = {
    (expectation - 3.0) <= result && result <= expectation + 3.0
  }
}
