package com.ghurtchu
package csv.service

import csv._

private[csv] class ContentBuilder(headers: Headers, rows: Rows) {

  def content: Content = Content {
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

private[csv] object ContentBuilder {

  def apply(headers: Headers, rows: Rows): ContentBuilder =
    new ContentBuilder(headers, rows)
}
