package com.ghurtchu
package csv.service

import csv.api.CanPrettify
import csv._

import scala.collection.mutable

private[csv] class CSVStringifier(private val csvColumnSelector: ColumnService,
                                  private val headers: Headers,
                                  private val rows: Rows) extends CanPrettify {

  override def stringify: String = {
    val maxLengthPerColumn: Vector[Int] = headers.values.map(_.value).map { header =>
      csvColumnSelector.column(header)
        .fold(0) { col =>
          (header +: col.cells
            .map(_.value))
            .maxBy(_.length)
            .length
        }
    }
    val concatenatedHeaders: String = maxLengthPerColumn.zip(headers.values).map { case (length, header) =>
      header.value concat (" " * (length - header.value.length)) concat " | "
    }.reduce(_ concat _)
    val stringifiedRows: Vector[Vector[String]] = rows.values.map(_.cells.map(_.value))
    val concatenatedRows: String = stringifiedRows.map { rows =>
      (for (i <- rows.indices) yield {
        val cell = rows(i)
        val length = maxLengthPerColumn(i)

        cell concat " " * (length - cell.length) concat " | "
      }).reduce(_ concat _)
    }.reduce(_ concat "\n" concat _)

    concatenatedHeaders
      .concat("\n".concat("-" * (concatenatedHeaders.length - 1)).concat("\n"))
      .concat(concatenatedRows)
  }
}

private[csv] object CSVStringifier {

  def apply(csvColumnSelector: ColumnService): CSVStringifier =
    new CSVStringifier(csvColumnSelector, csvColumnSelector.csv.headers, csvColumnSelector.csv.rows)
}
