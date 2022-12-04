package com.ghurtchu
package csv.impl

import csv.api.CanBuildContent

import csv.element
import csv.element.{Content, Headers, Rows}

private[csv] class CSVContentBuilder(headers: Headers, rows: Rows) extends CanBuildContent {

  override def content: element.Content = Content {
      val headersSeparated = headers.values.map(_.value).reduce((h1, h2) => h1 concat "," concat h2)
      val rowsList = rows.values.map { row =>
        row.cells
          .map(_.value)
          .reduce((c1, c2) => c1 concat "," concat c2) concat "\n"
      }.reduce(_ concat _)

      headersSeparated
        .concat("\n")
        .concat(rowsList)
    }
}

private[csv] object CSVContentBuilder {
  def instance(headers: Headers, rows: Rows): CanBuildContent =
    new CSVContentBuilder(headers, rows)
}
