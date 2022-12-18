package com.ghurtchu
package csv.service

import csv.api.CanSelectColumns
import csv._

import scala.annotation.tailrec
import scala.util.Try

private[csv] class ColumnService(val csv: CSV) extends CanSelectColumns {

  override def column(name: String): Option[Column] = {
    if (!csv.headers.values.map(_.value).contains(name)) None
    else {
      val header = Header(name)
      val count = csv.headerPlaceMapping(header)
      val cells = csv.rows.values.map(_.cells(count))

      Some(Column(header, cells))
    }
  }

  override def columns(names: String*): Either[Throwable, Columns] = Try {
    Columns {
      names
        .toVector
        .flatMap(column)
    }
  }.toEither

  def keepColumns(names: String*): Either[Throwable, CSV] =
    CSV(Headers(names.map(Header.apply).toVector), csv.rows)

  def keepColumns(range: Range): Either[Throwable, CSV] = {
    val keptHeaders = Headers(csv.headers.slice(range.start, range.end + 1))
    val keptRows = Rows(csv.rows.mapRows(row => Row(row.filterCells(cell => keptHeaders.contains(cell.header)))))

    CSV(keptHeaders, keptRows)
  }

  def dropColumns(names: String*): Either[Throwable, CSV] =
    keepColumns(csv.headers.valuesAsString.toSet.diff(names.toSet).toSeq: _*)

  def dropColumns(range: Range): Either[Throwable, CSV] = {
    val keptHeaders = Headers(csv.headers.slice(0, range.start) ++ csv.headers.slice(range.end + 1, csv.headers.count))
    val keptColumns = Rows(csv.rows.mapRows(row => Row(row.filterCells(cell => keptHeaders.contains(cell.header)))))

    CSV(keptHeaders, keptColumns)
  }

  def transformColumns(transformer: Column => Column): Either[Throwable, CSV] =
      columns(csv.headers.values.map(_.value): _*)
        .fold(Left.apply, col => Columns(col.values.map(transformer)).toCSV)

  def transformColumn(name: String)(f: Column => Column): Either[Throwable, CSV] =
    columns(csv.headers.values.map(_.value): _*)
      .fold(Left.apply, cols => {
        Columns {
          cols.values.map { col =>
            if (col.header.value == name) {
              val newCol = f(col)

              newCol.copy(cells = newCol.cells.map(_.copy(header = newCol.header)))
            }
            else col
          }
        }.toCSV
      })

  def transformColumns(pipe: TransformColumnPipe): Either[Throwable, CSV] = {
    val cols = columns(csv.headers.values.map(_.value): _*)

    @tailrec
    def loop(currCSV: CSV, currColumns: Columns, currPipe: TransformColumnPipe): Either[Throwable, CSV] = {
      if (currPipe.functions.isEmpty) Right(currCSV)
      else {
        val newCols = Columns(currColumns.values.map(currPipe.functions.head))

        newCols.toCSV match {
          case Left(err) => Left(err)
          case Right(csv) => loop(csv, newCols, TransformColumnPipe(currPipe.functions.tail: _*))
        }
      }
    }

    cols.fold(Left.apply, loop(csv, _, pipe))
  }

  def filterColumns(pipe: FilterColumnPipe): Either[Throwable, CSV] = {
    val cols = columns(csv.headers.map(_.value): _*)

    @tailrec
    def loop(currCSV: CSV, currColumns: Columns, currPipe: FilterColumnPipe): Either[Throwable, CSV] = {
      if (currPipe.functions.isEmpty) Right(currCSV)
      else {
        val newColumns = Columns(currColumns.values.filter(currPipe.functions.head))

        newColumns.toCSV match {
          case Left(err) => Left(err)
          case Right(csv) => loop(csv, newColumns, FilterColumnPipe(currPipe.functions.tail: _*))
        }
      }
    }

    cols.fold(Left.apply, loop(csv, _, pipe))
  }

  def filterColumns(predicate: Column => Boolean): Either[Throwable, CSV] =
    columns(csv.headers.valuesAsString: _*)
      .fold(Left.apply, cols => Columns(cols.filter(predicate)).toCSV)

  def addColumn(name: String, values: Seq[String]): Either[Throwable, CSV] = {
    val newHeader = Header(name)
    lazy val NAs = Vector.fill(csv.rows.size + 1)("N/A")
    val colElems = if (values.nonEmpty) values else NAs
    val newRows = Rows {
      csv.rows.values.zip(colElems).map { case (row, cellString) =>
        Row(row.cells :+ Cell(row.index, newHeader, cellString))
      }
    }

    CSV(csv.headers :+ newHeader, newRows)
  }


}

private[csv] object ColumnService {

  def apply(csv: CSV): ColumnService =
    new ColumnService(csv)
}
