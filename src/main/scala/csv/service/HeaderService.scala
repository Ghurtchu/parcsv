package com.ghurtchu
package csv.service

import csv._

final case class HeaderService(val csv: CSV) {

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
}
