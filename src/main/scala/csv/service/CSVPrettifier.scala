package com.ghurtchu
package csv.service

import csv.api.CanPrettify
import csv._

private[csv] class CSVPrettifier(private val csvColumnSelector: CSVColumnSelector,
                                 private val headers: Headers,
                                 private val rows: Rows) extends CanPrettify {

  override def prettify: String = {

    val maxLengthPerColumn: List[Int] = headers.values.map(_.value).map { header =>
      csvColumnSelector.column(header)
        .fold(0) { col =>
          (header :: col.cells
            .map(_.value))
            .maxBy(_.length)
            .length
        }
    }

    val concatenatedHeaders: String = maxLengthPerColumn.zip(headers.values).map { case (length, header) =>

      header.value concat (" " * (length - header.value.length)) concat " | "
    }.reduce(_ concat _)

    val stringifiedRows: List[List[String]] = rows.values.map(_.cells.map(_.value))

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

private[csv] object CSVPrettifier {

  def apply(csvColumnSelector: CSVColumnSelector): CSVPrettifier =
    new CSVPrettifier(csvColumnSelector, csvColumnSelector.headers, csvColumnSelector.rows)
}
