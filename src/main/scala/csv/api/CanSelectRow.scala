package com.ghurtchu
package csv.api

private[csv] trait CanSelectRow {

  def row(index: Int): Option[Row]

  def rows(range: Range): Either[Throwable, Rows]

  def rows(indices: Int*): Either[Throwable, Rows]

}
