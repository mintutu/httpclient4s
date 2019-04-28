package com.mintutu.httpclient4s.selector

import com.mintutu.httpclient4s.NoAvailableServer

class WeightedRoundRobinResource[T](items: List[T], defaultWeight: Int, decrementBy: Int) {

  private def getResources(): List[(T, Int)] = {
    items.map(e => (e, defaultWeight))
  }

  private var weightedRRItr = new WeightedRoundRobinIterator(getResources)

  def next: Option[T] = weightedRRItr.next

  def reset(resource: T): Unit = {
    val newItem = weightedRRItr.itemsToWeights.find(item => item._1 == resource && item._2 != defaultWeight)
      .map(e => (e._1, defaultWeight))
    newItem.map {
      item => set(weightedRRItr.itemsToWeights.filterNot(_._1 == resource) :+ item)
    }
  }

  def decrement(resource: T): Unit = {
    weightedRRItr.itemsToWeights.find(_._1 == resource) match {
      case Some(item) =>
        val newWeight = item._2 - decrementBy
        if (newWeight <= 0) {
          set(weightedRRItr.itemsToWeights.filterNot(_._1 == resource))
        } else {
          set(weightedRRItr.itemsToWeights.filterNot(_._1 == resource) :+ (resource, newWeight))
        }
      case None =>
        throw new NoAvailableServer(s"Not found $resource")
    }
  }

  def set(resources: List[(T, Int)]): Unit = {
    this.synchronized {
      weightedRRItr = new WeightedRoundRobinIterator(resources)
    }
  }
}
