package com.ghurtchu

import scala.util.Try

package object csv {

  sealed trait Mergeable

  implicit class MergeableOps(self: Mergeable) {
    def <+>(that: Mergeable): Either[Throwable, CSV] = (self, that) match {
      case (Headers(h), Rows(r)) => CSV(Headers(h), Rows(r))
      case (Rows(r), Headers(h)) => CSV(Headers(h), Rows(r))
      case _ => Left(new RuntimeException("Merge operation can not be performed"))
    }
  }

  final case class Cell(index: Int, header: Header, value: String) {
    override def toString: String = value
  }

  final case class Row(cells: List[Cell]) {
    override def toString: String = {
      val repr = cells.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      s"[$reprNormalized]"
    }
  }

  final case class Rows(values: List[Row]) extends Mergeable {
    override def toString: String = {
      val repr = values.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      reprNormalized.split("], ").mkString("]\n")
    }

    def filter(f: Cell => Boolean): Either[Throwable, Rows] = Try {
      Rows {
        values.filter { row =>
          row.cells.exists(f)
        }
      }
    }.toEither

    def toCSV(headers: Headers): Either[Throwable, CSV] =
      CSV.apply(headers, this)
  }

  final case class Header(value: String) {
    override def toString: String = value
  }

  final case class Headers(values: List[Header]) extends Mergeable {
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

      CSV.apply(Headers(headers), Rows(rows))
    }
  }

}
