package com.ghurtchu
package csv.impl

import csv.api.CanPrettify

import csv.element.{Headers, Rows}

private[csv] class CSVPrettifier(private val csvColumnSelector: CSVColumnSelector,
                                 private val headers: Headers,
                                 private val rows: Rows) extends CanPrettify {
  override def prettify: String = {
    val maxLengthPerColumn = headers.values.map(_.value).map { header =>
      csvColumnSelector.column(header)
        .fold(0) { col =>
          col.cells
            .map(_.value)
            .maxBy(_.length)
            .length
        }
    }

    val concatenatedHeaders: String = maxLengthPerColumn.zip(headers.values).map { tup =>
      val length = tup._1
      val header = tup._2

      header.value concat (" " * (length - header.value.length)) concat " | "
    }.reduce(_ concat _)

    val stringifiedRows: List[List[String]] = rows.values.map(_.cells.map(_.value))

    val concatenatedRows: String = (stringifiedRows.zip(List.fill(stringifiedRows.size)(maxLengthPerColumn)).map { tup =>
      val row = tup._1
      val lengths = tup._2

      (for (i <- row.indices) yield {
        val cell = row(i)
        val length = lengths(i)

        cell + " " * (length - cell.length) + " | "
      }).reduce(_ concat _)
    }).reduce((row1, row2) => row1 concat "\n" concat row2)


    concatenatedHeaders concat "\n" concat "-" * (concatenatedHeaders.length - 1) concat "\n" concat concatenatedRows
  }
}

private[csv] object CSVPrettifier {
  def instance(csvColumnSelector: CSVColumnSelector): CanPrettify =
    new CSVPrettifier(csvColumnSelector, csvColumnSelector.headers, csvColumnSelector.rows)
}
