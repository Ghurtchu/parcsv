package com.ghurtchu
package csv

object element {

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

  final case class Headers(values: List[Header]) {
    override def toString: String = {
      val repr = values.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      reprNormalized.split("], ").mkString("]\n")
    }
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
