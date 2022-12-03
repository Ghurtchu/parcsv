package com.ghurtchu
package csv.api

private[csv] trait CSVProtocol {

  import CSVProtocol._

  def content: Content

  def headers: List[Header]

  def rows: Rows

  def headerPlaceMapping: Map[Header, Int]

  def column(name: String): Option[Column]

  def at(rowIndex: Int, colIndex: Int): Option[Cell]

  def slice(rowRange: Range, colRange: Range): List[List[String]]
}

object CSVProtocol {

  final case class Cell(value: String) {
    override def toString: String = value
  }

  final case class Row(cells: List[Cell]) {
    override def toString: String = {
      val repr = cells.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      s"[$reprNormalized]"
    }
  }

  final case class Rows(values: List[Row]) {
    override def toString: String = {
      val repr = values.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)


      reprNormalized.split("], ").mkString("]\n")
    }
  }

  final case class Header(value: String) {
    override def toString: String = value
  }

  final case class Content(data: String) {
    override def toString: String = data
  }

  final case class Column(header: Header, cells: List[Cell]) {
    override def toString: String = {
      val repr = cells.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      s"$header: [$reprNormalized]"
    }
  }

}
