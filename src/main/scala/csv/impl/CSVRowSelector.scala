package com.ghurtchu
package csv.impl

import csv.api.CanSelectRow
import csv._

import scala.util.Try

private[csv] class CSVRowSelector(private val rows: Rows) extends CanSelectRow {

  override def row(index: Int): Option[Row] =
    Try(rows.values(index))
      .toOption

  override def withRows(range: Range): Either[Throwable, Rows] = Try {
    Rows {
      rows
        .values
        .slice(range.head, range.end)
    }
  }.toEither

  def rows(indices: Int*): Either[Throwable, Rows] = Try {
    Rows {
      rows.values
        .zipWithIndex.filter { case (_, ind) =>

        indices contains ind
      }.map(_._1)
    }
  }.toEither

}

private[csv] object CSVRowSelector {

  def apply(rows: Rows): CSVRowSelector =
    new CSVRowSelector(rows)
}
