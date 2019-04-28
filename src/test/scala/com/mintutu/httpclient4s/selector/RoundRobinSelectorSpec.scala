package com.mintutu.httpclient4s.selector

import akka.http.scaladsl.model.Uri
import org.scalatest.{FlatSpec, Matchers}

class RoundRobinSelectorSpec extends FlatSpec with Matchers with PercentageTestHelper {

  val TEST_SIZE = 5000

  "RoundRobinSelector.next" should "return evenly distributed items" in {
    val uri1 = Uri("http://192.168.0.1")
    val uri2 = Uri("http://192.168.0.2")
    val uri3 = Uri("http://192.168.0.3")
    val resource = new RoundRobinSelector() {
      override def hosts(): Seq[Uri] = Seq(uri1, uri2, uri3)
    }
    val result = (1 to TEST_SIZE).map(_ => resource.select())
    val groupedResult = result.flatten.toList.groupBy(identity).mapValues(e => e.size * 100.0 / TEST_SIZE)
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri1, 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri2, 0)) shouldBe true
    isUnderPercentAllocation(33, groupedResult.getOrElse(uri3, 0)) shouldBe true
  }
}
