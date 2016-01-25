package ru.osfb.webapi.core

/**
  * Created by sgl on 25.01.16.
  */
trait Lifecycle {
  def start(): Unit
  def stop(): Unit
}
