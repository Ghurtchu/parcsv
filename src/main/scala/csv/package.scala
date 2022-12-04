package com.ghurtchu

package object csv {

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

    def toCSV(headers: Headers): Either[Throwable, CSV] =
      CSV.fromHeadersAndRows(headers, this)
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

  final case class Columns(values: List[Column]) {

    def toCSV: Either[Throwable, CSV] = {
      val rowLength = values.head.cells.length
      val headers = values.map(_.header)
      val headerlessColumns = values drop 0
      val rows = (0 until rowLength).map { i =>
        headerlessColumns.map { col =>
          val cell = col.cells(i)

          cell
        }
      }.map(Row.apply).toList

      CSV.fromHeadersAndRows(Headers(headers), Rows(rows))
    }
  }

}
