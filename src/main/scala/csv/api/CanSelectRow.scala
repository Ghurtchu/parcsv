package com.ghurtchu
package csv.api

import csv.element.{Row, Rows}

private[csv] trait CanSelectRow {

  def row(index: Int): Option[Row]

  def rows(range: Range): Either[Throwable, Rows]

}
