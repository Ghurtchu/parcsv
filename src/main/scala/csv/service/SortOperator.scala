package com.ghurtchu
package csv.service

import csv.{CSV, Headers, Row, Rows, SortOrdering}

class SortOperator(val csv: CSV, val colName: String = "") {

  def sortByColumn(ordering: SortOrdering, isNumeric: Boolean): Either[Throwable, CSV] = {
    implicit val rowsOrdering: Ordering[Row] = SortOrdering.defineHeadersOrdering(colName, ordering, isNumeric)
    val sortedRows = Rows(csv.rows.values.sorted)

    CSV(csv.headers, sortedRows)
  }

  def sortHeaders(ordering: SortOrdering): Either[Throwable, CSV] = {
    implicit val (headersOrderng, cellsOrdering) = SortOrdering.fromSortOrder(ordering)
    val sortedHeaders = Headers(csv.headers.values.sorted)
    val sortedRows = Rows(csv.rows.mapRows(_.cells.sorted).map(Row.apply))

    CSV(sortedHeaders, sortedRows)
  }

}

object SortOperator {

  def apply(csv: CSV): SortOperator = new SortOperator(csv)

  def apply(csv: CSV, colName: String) = new SortOperator(csv, colName)

}
