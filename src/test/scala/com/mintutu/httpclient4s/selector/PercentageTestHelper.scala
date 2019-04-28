package com.mintutu.httpclient4s.selector

trait PercentageTestHelper {

  def isUnderPercentAllocation(expectation: Double, result: Double): Boolean = {
    (expectation - 3.0) <= result && result <= expectation + 3.0
  }
}
