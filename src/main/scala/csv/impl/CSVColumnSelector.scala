package com.ghurtchu
package csv.impl

import csv.api.CanSelectColumn
import csv.element._

private[csv] class CSVColumnSelector(val headerPlaceMapping: Map[Header, Int],
                                     val headers: Headers,
                                     val rows: Rows) extends CanSelectColumn {

  override def column(name: String): Option[Column] = {
    if (!headers.values.map(_.value).contains(name)) None
    else {
      val header = Header(name)
      val count = headerPlaceMapping(header)
      val cells = rows.values.map(_.cells(count))

      Some(Column(header, cells))
    }
  }
}

private[csv] object CSVColumnSelector {

  def apply(headerPlaceMapping: Map[Header, Int], headers: Headers, rows: Rows): CSVColumnSelector =
    new CSVColumnSelector(headerPlaceMapping, headers, rows)
}
