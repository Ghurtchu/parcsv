package com.ghurtchu
package csv.service

import csv.api.CanSelectColumns
import csv._

import scala.util.Try

private[csv] class ColumnSelector(val csv: CSV) extends CanSelectColumns {

  override def column(name: String): Option[Column] = {
    if (!csv.headers.values.map(_.value).contains(name)) None
    else {
      val header = Header(name)
      val count = csv.headerPlaceMapping(header)
      val cells = csv.rows.values.map(_.cells(count))

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

  def apply(csv: CSV): ColumnSelector =
    new ColumnSelector(csv)
}
