package com.ghurtchu
package csv.impl

import csv.api.CanSelectColumns
import csv.element._

import scala.util.Try

private[csv] class CSVColumnSelector(val headerPlaceMapping: Map[Header, Int],
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

  override def columns(names: String*): Either[Throwable, Columns] =
    Try(Columns(names.toList.flatMap(column)))
      .toEither

}

private[csv] object CSVColumnSelector {

  def apply(headerPlaceMapping: Map[Header, Int], headers: Headers, rows: Rows): CSVColumnSelector =
    new CSVColumnSelector(headerPlaceMapping, headers, rows)
}
