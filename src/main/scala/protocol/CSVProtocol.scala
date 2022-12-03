package com.ghurtchu
package protocol

private[protocol] trait CSVProtocol {
  def content: Content

  def headers: List[Header]

  def rows: Rows

  def headerPlaceMapping: Map[Header, Int]

  def column(name: String): Option[Column]

  def at(rowIndex: Int, colIndex: Int): Option[Cell]

  def slice(rowRange: Range, colRange: Range): List[List[String]]
}

object CSVProtocol {

}
