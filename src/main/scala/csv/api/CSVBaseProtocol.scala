package com.ghurtchu
package csv.api

private[csv] trait CSVBaseProtocol {

  def headers: List[Header]

  def rows: Rows

  def content: Content

  def column(name: String): Option[Column]

  def at(rowIndex: Int, colIndex: Int): Option[Cell]

  def slice(rowRange: Range, colRange: Range): List[List[String]]

  protected def headerPlaceMapping: Map[Header, Int]

}
