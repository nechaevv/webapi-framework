package ru.osfb.webapi.db

import java.sql.Timestamp
import java.time.Instant

import slick.driver.JdbcDriver

/**
 * Created by v.a.nechaev on 05.05.2015.
 */
trait DatabaseComponent { this: DatabaseDriverComponent =>
  def database: databaseDriver.api.Database
}

trait DatabaseDriverComponent {
  val databaseDriver: JdbcDriver
}

trait BaseColumnTypes { this: DatabaseDriverComponent =>
  import databaseDriver.api._
  implicit lazy val instantColumnType = MappedColumnType.base[Instant, Timestamp](
    i => Timestamp.from(i),
    t => t.toInstant
  )
}