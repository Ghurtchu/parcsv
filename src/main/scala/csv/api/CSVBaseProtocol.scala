package com.ghurtchu
package csv.api

private[csv] trait CSVBaseProtocol {

  def headers: Headers

  def rows: Rows // could have also had "columns" but I prefer rows

  def content: Content

  def column(name: String): Option[Column]

  def row(index: Int): Option[Row]

  def at(rowIndex: Int, colIndex: Int): Option[Cell]

  def slice(rowRange: Range, colRange: Range): List[List[String]]

  protected def headerPlaceMapping: Map[Header, Int]

}
