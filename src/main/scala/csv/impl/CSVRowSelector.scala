package com.ghurtchu
package csv.impl

import csv.api.CanSelectRow
import csv.element.{Row, Rows}

import scala.util.Try

private[csv] class CSVRowSelector(private val rows: Rows) extends CanSelectRow {

  override def row(index: Int): Option[Row] =
    Try(rows.values(index))
      .toOption

  override def rows(range: Range): Either[Throwable, Rows] = Try {
    Rows {
      rows
        .values
        .slice(range.head, range.end)
    }
  }.toEither
}

private[csv] object CSVRowSelector {

  def apply(rows: Rows): CSVRowSelector =
    new CSVRowSelector(rows)
}
