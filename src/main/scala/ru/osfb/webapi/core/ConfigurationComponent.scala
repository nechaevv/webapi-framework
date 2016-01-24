package ru.osfb.webapi.core

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.FiniteDuration

/**
 * Created by sgl on 26.04.15.
 */
trait ConfigurationComponent {
  def configuration: Config
  implicit def java2scalaDuration(d: java.time.Duration): FiniteDuration = {
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)
  }
}

trait ConfigurationComponentImpl extends ConfigurationComponent {
  lazy val configuration = ConfigFactory.load()
}