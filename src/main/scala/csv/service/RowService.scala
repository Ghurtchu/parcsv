package com.ghurtchu
package csv.service

import csv._

import scala.annotation.tailrec
import scala.util.Try

private[csv] class RowService(val csv: CSV) {

  def row(index: Int): Option[Row] =
    Try(csv.rows.values(index))
      .toOption

  def withRows(range: Range): Either[Throwable, Rows] = Try {
    Rows {
      csv.rows
        .values
        .slice(range.head, range.end)
    }
  }.toEither

  def rows(indices: Int*): Either[Throwable, Rows] = Try {
    Rows {
      csv.rows.values
        .zipWithIndex.filter { case (_, ind) =>

        indices contains ind
      }.map(_._1)
    }
  }.toEither


  def keepRows(range: Range): Either[Throwable, CSV] =
    withRows(range)
      .fold(Left.apply, CSV(csv.headers, _))

  def keepRows(indices: Int*): Either[Throwable, CSV] =
    rows(indices: _*)
      .fold(Left.apply, CSV(csv.headers, _))

  def dropRows(range: Range): Either[Throwable, CSV] = {
    val newRows = Rows {
      csv.rows.filterRows { row =>
        val index = row.cells.head.index

        index < range.start || index > range.end
      }
    }

    CSV(csv.headers, newRows)
  }

  def dropRows(indices: Int*): Either[Throwable, CSV] = {
    val keptRows = Rows {
      csv.rows.values.zipWithIndex.filter { case (_, ind) =>
        !(indices contains ind)
      }.map(_._1)
    }

    CSV(csv.headers, keptRows)
  }

  def filterRows(predicate: Cell => Boolean): Either[Throwable, CSV] =
    CSV(csv.headers, Rows(csv.rows.filterRows(_.cells.exists(predicate))))


  def filterRows(pipe: FilterRowPipe): Either[Throwable, CSV] = {

    @tailrec
    def loop(currCSV: CSV, currRows: Rows, currPipe: FilterRowPipe): Either[Throwable, CSV] = {
      if (currPipe.functions.isEmpty) Right(currCSV)
      else {
        val newRows = Rows(currRows.values.filter(currPipe.functions.head))
        val newHeaders = Headers(newRows.values.head.cells.map(_.header))

        CSV(newHeaders, newRows) match {
          case Right(csv) => loop(csv, newRows, FilterRowPipe(currPipe.functions.tail: _*))
          case Left(err) => Left(err)
        }
      }
    }

    loop(csv, csv.rows, pipe)
  }

  def addRow(values: Seq[String]): Either[Throwable, CSV] = {
    val newRow = Row {
      values.toVector.zip(csv.headers.values).map { case (value, header) =>
        Cell(csv.rows.size, header, value)
      }
    }

    CSV(csv.headers, csv.rows :+ newRow)
  }

  def mapRows[A](transformer: Row => A): Either[Throwable, Vector[A]] =
    Try(csv.rows.mapRows(transformer)).toEither

}

private[csv] object RowService {

  def apply(implicit csv: CSV): RowService =
    new RowService(csv)
}
