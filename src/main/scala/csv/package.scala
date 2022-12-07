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

//  implicit class ListPipelineOps(self: List[Pipeline]) {
//    def ~>(that: Pipeline): List[Pipeline] = self :+ that
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
    def tail: Seq[A => B]
  }

  final case class TransformColumnPipe(override val functions: Column => Column*) extends Pipe[Column, Column] {
    override def tail: Seq[Column => Column] = functions.tail
  }

  final case class FilterColumnPipe(override val functions: Column => Boolean*) extends   Pipe[Column, Boolean] {
    override def tail: Seq[Column => Boolean] = functions.tail
  }

  final case class FilterRowPipe(override val functions: Row => Boolean*) extends         Pipe[Row, Boolean] {
    override def tail: Seq[Row => Boolean] = functions.tail
  }

}
