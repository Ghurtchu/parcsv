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

    def headerValue: String = header.value

    def belongsTo(columnName: String): Boolean = header.value == columnName

    def numericValue: Double = value.toDouble

  }

  final case class Row(cells: Vector[Cell]) {

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

  final case class Rows(values: Vector[Row]) extends Mergeable {

    override def toString: String = {
      val repr = values.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      reprNormalized.split("], ").mkString("]\n")
    }

    def size: Int = values.size

    def filter(f: Cell => Boolean): Either[Throwable, Rows] = Try {
      Rows {
        values.filter { row =>
          row
            .cells
            .exists(f)
        }
      }
    }.toEither

    def :+(row: Row): Rows = Rows(values :+ row)

  }

  final case class Header(value: String) {
    override def toString: String = value
  }

  final case class Headers(values: Vector[Header]) extends Mergeable {

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

    def :+(header: Header): Headers = Headers(values :+ header)

  }

  final case class Content(data: String) {
    override def toString: String = data
  }

  final case class Column(header: Header, cells: Vector[Cell]) {

    override def toString: String = {
      val repr = cells.map(_.toString).toString
      val reprNormalized = repr.substring(5, repr.length - 1)

      s"$header: [$reprNormalized]"
    }

    def mapHeader(f: Header => Header): Column = Column(f(header), cells.map(_.copy(header = f(header))))

    def mapCells(f: Vector[Cell] => Vector[Cell]): Column = {
      val newCells = f(cells)
      val newHeader = header.copy(newCells.head.value)

      Column(newHeader, newCells)
    }

  }

  final case class Columns(values: Vector[Column]) {

    def toCSV: Either[Throwable, CSV] = {
      val rowLength = values.head.cells.length
      val headers = values.map(_.header)
      val headerlessColumns = values drop 0
      val rows = (0 until rowLength).map { i =>
        headerlessColumns.map { col =>
          val cell = col.cells(i)

          cell
        }
      }.map(Row.apply).toVector

      CSV.apply(Headers(headers), Rows(rows))
    }

  }

//  implicit class VectorPipelineOps(self: Vector[Pipeline]) {
//    def ~>(that: Pipeline): Vector[Pipeline] = self :+ that
//  }

  implicit class SeqPipelineOps(selves: Seq[UntypedPipe]) {
    def ~>(that: UntypedPipe): Seq[UntypedPipe] = selves :+ that
    def ++(them: Seq[UntypedPipe]): Seq[UntypedPipe] = selves ++ them
  }

  implicit class PipelineOps(self: UntypedPipe) {
    def ~>(that: UntypedPipe): Seq[UntypedPipe] = self :: that :: Nil
    def ++(them: Seq[UntypedPipe]): Seq[UntypedPipe] = self +: them
  }

  type UntypedPipe = Pipe[_, _]

  sealed trait Pipe[-A, +B] {
    def functions: Seq[A => B]
  }

  final case class TransformColumnPipe(override val functions: Column => Column*) extends Pipe[Column, Column]
  final case class FilterColumnPipe(override val functions: Column => Boolean*) extends   Pipe[Column, Boolean]
  final case class FilterRowPipe(override val functions: Row => Boolean*) extends         Pipe[Row, Boolean]

}
