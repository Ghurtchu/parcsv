package com.ghurtchu

import java.nio.MappedByteBuffer
import scala.util.Try

package object csv {

  sealed trait Mergeable

  implicit class MergeableOps(self: Mergeable) {

    def <+>(that: Mergeable): Either[Throwable, CSV] = (self, that) match {
      case (Headers(h), Rows(r)) => CSV(Headers(h), Rows(r))
      case (Rows(r), Headers(h)) => CSV(Headers(h), Rows(r))
      case _                     => Left(new RuntimeException("Merge operation can not be performed"))
    }
  }

  final case class Cell(index: Int, header: Header, value: String) {

    override def toString: String = value

    def isNumeric: Boolean = Try(value.toDouble).isSuccess || Try(value.toInt).isSuccess

  }

  final case class Row(cells: List[Cell]) {

    override def toString: String = {
      val repr = cells.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      s"[$reprNormalized]"
    }

    def index: Int = cells.head.index

    def isFull: Boolean = cells.forall { cell =>
      val value = cell.value.trim

      value != "" && value != "N/A"
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
          row
            .cells
            .exists(f)
        }
      }
    }.toEither
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

    def filter(f: Header => Boolean): Either[Throwable, Headers] = Try {
      Headers {
        values
          .filter(f)
      }
    }.toEither
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

  sealed trait Pipeline

//  abstract sealed class TransformPipe[A, B] extends Pipeline[A, B] {
//    def isEmpty: Boolean = functions.isEmpty
//
//    def head: A => B = functions.head
//
//    def tail: TransformPipe[A, B]
//  }

  final case class TransformColumnPipe(functions: Column => Column*) extends Pipeline {
    def tail: Pipeline = TransformColumnPipe(functions.tail: _*)
  }

//  abstract sealed class FilterPipe[A] extends Pipeline[A, Boolean] {
//    def isEmpty: Boolean = functions.isEmpty
//
//    def head: A => Boolean = functions.head
//
//    def tail: FilterPipe[A]
//  }

  final case class FilterColumnPipe(functions: Column => Boolean*) extends Pipeline {
    def tail: FilterColumnPipe = FilterColumnPipe(functions.tail: _*)
    def isEmpty: Boolean = functions.isEmpty
    def head: Column => Boolean = functions.head
  }

  final case class FilterRowPipe(functions: Row => Boolean*) extends Pipeline {
    def tail: FilterRowPipe = FilterRowPipe(functions.tail: _*)
    def isEmpty: Boolean = functions.isEmpty
    def head: Row => Boolean = functions.head
  }

}
