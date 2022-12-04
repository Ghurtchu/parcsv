package com.ghurtchu
package csv.api

private[csv] trait CSVBaseProtocol {

  def content: Content

  def headers: List[Header]

  def rows: Rows

  def column(name: String): Option[Column]

  def at(rowIndex: Int, colIndex: Int): Option[Cell]

  def slice(rowRange: Range, colRange: Range): List[List[String]]

  protected def headerPlaceMapping: Map[Header, Int]

}
