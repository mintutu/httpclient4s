package com.mintutu.httpclient4s.selector

import scala.util.Random

class WeightedRoundRobinIterator[T](val itemsToWeights: List[(T, Int)]) {

  val totalWeights = itemsToWeights.filter(_._2 > 0).map(_._2).sum

  def next = {
    if (totalWeights > 0) {
      val seed = Random.nextInt(totalWeights)
      var runningSum: Long = 0
      itemsToWeights
        .sortBy(_._2)
        .reverse
        .find((resource) => {
          runningSum += resource._2
          runningSum > seed
        }).map(_._1)
    }
    else {
      None
    }
  }
}
