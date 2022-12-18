package com.ghurtchu
package csv.service

import csv.api.CanSelectColumns
import csv._

import scala.util.Try

private[csv] class ColumnSelector(val headerPlaceMapping: Map[Header, Int],
                                  val headers: Headers,
                                  val rows: Rows) extends CanSelectColumns {

  override def column(name: String): Option[Column] = {
    if (!headers.values.map(_.value).contains(name)) None
    else {
      val header = Header(name)
      val count = headerPlaceMapping(header)
      val cells = rows.values.map(_.cells(count))

      Some(Column(header, cells))
    }
  }

  override def columns(names: String*): Either[Throwable, Columns] = Try {
    Columns {
      names
        .toVector
        .flatMap(column)
    }
  }.toEither

}

private[csv] object ColumnSelector {

  def apply(headerPlaceMapping: Map[Header, Int], headers: Headers, rows: Rows): ColumnSelector =
    new ColumnSelector(headerPlaceMapping, headers, rows)
}
