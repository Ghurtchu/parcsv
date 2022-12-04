package com.ghurtchu
package csv

object element {

  final case class Cell(value: String) {
    override def toString: String = value
  }

  object Cell {
    def empty: Cell = new Cell("")
  }

  final case class Row(cells: List[Cell]) {
    override def toString: String = {
      val repr = cells.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      s"[$reprNormalized]"
    }
  }

  object Row {
    def empty: Row = new Row(Nil)
  }

  final case class Rows(values: List[Row]) {
    override def toString: String = {
      val repr = values.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      reprNormalized.split("], ").mkString("]\n")
    }
  }

  object Rows {
    def empty: Rows = new Rows(Nil)
  }

  final case class Header(value: String) {
    override def toString: String = value
  }

  object Header {
    def empty: Header = new Header("")
  }

  final case class Headers(values: List[Header]) {
    override def toString: String = {
      val repr = values.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      reprNormalized.split("], ").mkString("]\n")
    }
  }

  object Headers {
    def empty: Headers = new Headers(Nil)
  }

  final case class Content(data: String) {
    override def toString: String = data
  }

  object Content {
    def empty: Content = new Content("")
  }

  final case class Column(header: Header, cells: List[Cell]) {
    override def toString: String = {
      val repr = cells.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      s"$header: [$reprNormalized]"
    }
  }

  object Column {
    def empty: Column = new Column(Header.empty, Nil)
  }

}
