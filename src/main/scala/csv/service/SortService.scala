package com.ghurtchu
package csv.service

import csv.{CSV, Headers, Row, Rows, SortOrdering}

class SortService(val csv: CSV, val colName: String = "") {

  def sortByColumn(ordering: SortOrdering): Either[Throwable, CSV] = {
    val isNumeric = csv.rows.values.flatMap(_.cells.find(_.header.value == colName)).forall(_.isNumeric)
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

object SortService {

  def apply(implicit csv: CSV): SortService = new SortService(csv)

  def apply(colName: String)(implicit csv: CSV) = new SortService(csv, colName)

}
