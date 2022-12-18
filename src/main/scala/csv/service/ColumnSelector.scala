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

  def keepColumns(names: String*): Either[Throwable, CSV] =
    CSV(Headers(names.map(Header.apply).toVector), csv.rows)

  def keepColumns(range: Range): Either[Throwable, CSV] = {
    val keptHeaders = Headers(csv.headers.slice(range.start, range.end + 1))
    val keptRows = Rows(csv.rows.mapRows(row => Row(row.filterCells(cell => keptHeaders.contains(cell.header)))))

    CSV(keptHeaders, keptRows)
  }

  def dropColumns(names: String*): Either[Throwable, CSV] =
    keepColumns(csv.headers.valuesAsString.toSet.diff(names.toSet).toSeq: _*)

  def dropColumns(range: Range): Either[Throwable, CSV] = {
    val keptHeaders = Headers(csv.headers.slice(0, range.start) ++ csv.headers.slice(range.end + 1, csv.headers.count))
    val keptColumns = Rows(csv.rows.mapRows(row => Row(row.filterCells(cell => keptHeaders.contains(cell.header)))))

    CSV(keptHeaders, keptColumns)
  }


}

private[csv] object ColumnSelector {

  def apply(csv: CSV): ColumnSelector =
    new ColumnSelector(csv)
}
