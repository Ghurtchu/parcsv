package com.ghurtchu
package csv.service

import csv._

private[csv] final class HeaderService(val csv: CSV) {

  def mapHeaders(f: Header => String): Either[Throwable, CSV] = {
    val transformedHeaders = Headers(csv.headers.map(f andThen Header.apply))
    val transformedRows = Rows {
      csv.rows.mapRows { row =>
        Row {
          row.cells.zip(transformedHeaders.values).map { case (cell, header) =>
            cell.copy(header = header)
          }
        }
      }
    }

    CSV(transformedHeaders, transformedRows)
  }

  def filterHeaders(predicate: Header => Boolean): Either[Throwable, Headers] =
    csv.headers.filter(predicate)

}

private[csv] object HeaderService {
  def apply(implicit csv: CSV): HeaderService = new HeaderService(csv)
}


