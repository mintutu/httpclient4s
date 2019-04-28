package com.mintutu.httpclient4s.selector

trait Selector[T] {

  def hosts(): Seq[T]

  def select(): Option[T]

  def success(t: T): Unit

  def failure(t: T): Unit
}
