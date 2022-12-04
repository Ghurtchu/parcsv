package com.ghurtchu
package csv.api

import csv._

private[csv] trait CanSelectRow {

  def row(index: Int): Option[Row]

  def rows(range: Range): Either[Throwable, Rows]

  def rows(indices: Int*): Either[Throwable, Rows]

}
