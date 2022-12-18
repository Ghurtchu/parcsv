package com.ghurtchu
package csv

import csv.service._

import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.util.Try

final class CSV private (private[csv] val headers: Headers, private[csv] val rows: Rows) {

  private val headerPlaceMapping: Map[Header, Int] =
    headers
      .values
      .zipWithIndex
      .toMap

  def raw: Content =
    ContentBuilder(headers, rows)
      .content

  def keepColumns(names: String*): Either[Throwable, CSV] =
    CSV(Headers(names.map(Header.apply).toVector), rows)

  def keepColumns(range: Range): Either[Throwable, CSV] = {
    val keptHeaders = Headers(headers.slice(range.start, range.end + 1))
    val keptRows = Rows(rows.mapRows(row => Row(row.filterCells(cell => keptHeaders.contains(cell.header)))))

    CSV(keptHeaders, keptRows)
  }

  def dropColumns(names: String*): Either[Throwable, CSV] =
    keepColumns(headers.valuesAsString.toSet.diff(names.toSet).toSeq: _*)

  def dropColumns(range: Range): Either[Throwable, CSV] = {
    val keptHeaders = Headers(headers.slice(0, range.start) ++ headers.slice(range.end + 1, headers.count))
    val keptColumns = Rows(rows.mapRows(row => Row(row.filterCells(cell => keptHeaders.contains(cell.header)))))

    CSV(keptHeaders, keptColumns)
  }

  def filterHeaders(f: Header => Boolean): Either[Throwable, Headers] =
    headers filter f

  override val toString: String =
    CSVStringifier(ColumnSelector(headerPlaceMapping, headers, rows))
      .stringify

  def save(filePath: String = System.currentTimeMillis().toString concat ".csv"): Either[Throwable, Boolean] =
    CSVWriter(raw)
      .save(filePath)

  def filterColumns(f: Column => Boolean): Either[Throwable, CSV] =
    ColumnSelector(headerPlaceMapping, headers, rows)
      .columns(headers.valuesAsString: _*)
      .fold(Left.apply, cols => Columns(cols.filter(f)).toCSV)

  private def filterColumns(csv: CSV, pipe: FilterColumnPipe): Either[Throwable, CSV] = {
    val columns = ColumnSelector(csv.headerPlaceMapping, csv.headers, csv.rows)
      .columns(headers.map(_.value): _*)

    @tailrec
    def loop(currCSV: CSV, currColumns: Columns, currPipe: FilterColumnPipe): Either[Throwable, CSV] = {
      if (currPipe.functions.isEmpty) Right(currCSV)
      else {
        val newColumns = Columns(currColumns.values.filter(currPipe.functions.head))

        newColumns.toCSV match {
          case Left(err)  => Left(err)
          case Right(csv) => loop(csv, newColumns, FilterColumnPipe(currPipe.functions.tail: _*))
        }
      }
    }

    columns.fold(Left.apply, loop(csv, _, pipe))
  }

  def mapHeaders(f: Header => String): Either[Throwable, CSV] = {
    val transformedHeaders = Headers(headers.map(f andThen Header.apply))
    val transformedRows = Rows {
      rows.mapRows { row =>
        Row {
          row.cells.zip(transformedHeaders.values).map { case (cell, header) =>
            cell.copy(header = header)
          }
        }
      }
    }

    CSV(transformedHeaders, transformedRows)
  }

  def transformColumns(f: Column => Column): Either[Throwable, CSV] =
    ColumnSelector(headerPlaceMapping, headers, rows)
      .columns(headers.values.map(_.value): _*)
      .fold(Left.apply, col => Columns(col.values.map(f)).toCSV)

  def transformColumn(name: String)(f: Column => Column): Either[Throwable, CSV] =
    ColumnSelector(headerPlaceMapping, headers, rows)
      .columns(headers.values.map(_.value): _*)
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


  private def transformColumns(csv: CSV, pipe: TransformColumnPipe): Either[Throwable, CSV] = {
    val columns = ColumnSelector(csv.headerPlaceMapping, csv.headers, csv.rows)
      .columns(headers.values.map(_.value): _*)

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

    columns.fold(Left.apply, loop(csv, _, pipe))
  }

  def display: Either[Throwable, Unit] =
    Try(println(this)).toEither

  def keepRows(range: Range): Either[Throwable, CSV] =
    RowSelector(rows)
      .withRows(range)
      .fold(Left.apply, CSV(headers, _))

  def keepRows(indices: Int*): Either[Throwable, CSV] =
    RowSelector(rows)
      .rows(indices: _*)
      .fold(Left.apply, CSV(headers, _))

  def dropRows(range: Range): Either[Throwable, CSV] = {
    val newRows = Rows {
      rows.filterRows { row =>
        val index = row.cells.head.index

        index < range.start || index > range.end
      }
    }

    CSV(headers, newRows)
  }

  def sortHeaders(ordering: SortOrdering): Either[Throwable, CSV] =
    SortOperator(this)
      .sortHeaders(ordering)

  def sortByColumn(name: String, ordering: SortOrdering): Either[Throwable, CSV] = {
    val isNumeric = rows.values.flatMap(_.cells.find(_.header.value == name)).forall(_.isNumeric)
    SortOperator(this, name)
      .sortByColumn(ordering, isNumeric)
  }

  def dropRows(indices: Int*): Either[Throwable, CSV] = {
    val keptRows = Rows {
      rows.values.zipWithIndex.filter { case (_, ind) =>
        !(indices contains ind)
      }.map(_._1)
    }

    CSV(headers, keptRows)
  }

  def filterRows(f: Cell => Boolean): Either[Throwable, CSV] =
    CSV(headers, Rows(rows.filterRows(_.cells.exists(f))))

  private def filterRows(csv: CSV, pipe: FilterRowPipe): Either[Throwable, CSV] = {

    @tailrec
    def loop(currCSV: CSV, currRows: Rows, currPipe: FilterRowPipe): Either[Throwable, CSV] = {
      if (currPipe.functions.isEmpty) Right(currCSV)
      else {
        val newRows = Rows(currRows.values.filter(currPipe.functions.head))
        val newHeaders = Headers(newRows.values.head.cells.map(_.header))

        CSV(newHeaders, newRows) match {
          case Right(csv) => loop(csv, newRows, FilterRowPipe(currPipe.functions.tail: _*))
          case Left(err)  => Left(err)
        }
      }
    }

    loop(csv, csv.rows, pipe)
  }

  def transformVia(pipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {

    @tailrec
    def loop(csv: CSV, currPipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {
      if (currPipeline.isEmpty) Right(csv)
      else {
        val newCsv = currPipeline.head match {
          case FilterRowPipe(functions@_*)       => filterRows(csv, FilterRowPipe(functions: _*))
          case FilterColumnPipe(functions@_*)    => filterColumns(csv, FilterColumnPipe(functions: _*))
          case TransformColumnPipe(functions@_*) => transformColumns(csv, TransformColumnPipe(functions: _*))
        }

        newCsv match {
          case Left(err)  => Left(err)
          case Right(csv) => loop(csv, currPipeline.tail)
        }
      }
    }

    loop(this, pipeline)
  }

  def addRow(values: Seq[String]): Either[Throwable, CSV] = {
    val newRow = Row {
      values.toVector.zip(headers.values).map { case (value, header) =>
        Cell(rows.size, header, value)
      }
    }

    CSV(headers, rows :+ newRow)
  }

  def addColumn(name: String, cells: Seq[String] = List.empty): Either[Throwable, CSV] = {
    val newHeader = Header(name)
    lazy val NAs = Vector.fill(rows.size + 1)("N/A")
    val colElems = if (cells.nonEmpty) cells else NAs
    val newRows = Rows {
      rows.values.zip(colElems).map { case (row, cellString) =>
        Row(row.cells :+ Cell(row.index, newHeader, cellString))
      }
    }

    CSV(headers :+ newHeader, newRows)
  }

  def mapRows[A](f: Row => A): Either[Throwable, Vector[A]] =
    Try(rows.mapRows(f)).toEither

}

object CSV {

  import scala.util._
  import scala.io.Source.{fromFile => read}

  def fromString(csvContent: String): Either[Throwable, CSV] =
    Try(new CSV(extractHeaders(csvContent), extractRows(csvContent))).toEither

  def fromFile(path: String): Either[Throwable, CSV] = Try {
    val file = read(path)
    val csvContent = file.mkString.replace("\r", "")
    file.close()
    val headers = extractHeaders(csvContent)
    val rows = extractRows(csvContent)

    new CSV(headers, rows)
  }.toEither

  def fromMap(map: Map[String, Seq[String]]): Either[Throwable, CSV] = Try {
    val listMap = ListMap.from(map)
    val headers = Headers(listMap.keys.map(Header.apply).toVector)
    val stringRows = (listMap.head._2.indices by 1).map { i => listMap.values.map(cols => cols(i)) }
    val rows = Rows {
      (for (i <- stringRows.indices) yield {
        val stringCells = stringRows(i)
        stringCells.zip(headers.values).map { case (strCell, header) =>

          Cell(i, header, strCell)
        }.toVector
      }).map(Row.apply).toVector
    }

    new CSV(headers, rows)
  }.toEither

  // I know it's mutable, be calm purist, it's a local function, it's real world baby!
  private [csv] def apply(headers: Headers, rows: Rows): Either[Throwable, CSV] = {
    val rowsBuffer = collection.mutable.Queue.empty[Row]
    rows.values.foreach { row =>
      val sortedCells = collection.mutable.Stack.empty[Cell]
      val copiedCells = collection.mutable.Stack(row.cells: _*)
      headers.values.foreach { header =>
        copiedCells.foreach { cell =>
          if (cell.header == header && !sortedCells.contains(cell)) {
            sortedCells append cell
          }
        }
      }
      rowsBuffer.enqueue(Row(sortedCells.toVector))
    }

    Try(new CSV(headers, Rows(rowsBuffer.toVector))).toEither
  }

  private def extractHeaders(csv: String): Headers = Headers(csv.takeWhile(_ != '\n').split(",").map(Header.apply).toVector)

  private def extractRows(csv: String): Rows = {
    val headers: Array[Header] = csv.takeWhile(_ != '\n').split(",").map(Header.apply)
    val rows: Rows = Rows {
      csv.dropWhile(_ != '\n')
        .tail
        .split("\n")
        .toVector
        .map { rawLine =>
          val splitted: Array[String] = rawLine.split("\"").filter(_.trim != "")
          if (splitted.length > 1) {
            splitted.flatMap { line =>
              if (isComplexString(line)) Array(line)
              else line.split(",").filter(s => s.trim != "," && s.trim != "")
            }
          } else rawLine.split(",").filter(_.trim != "")
        }.zipWithIndex.map { case (line, index) =>
        line.zip(headers).map { case (word, header) =>
          // put string in "quotes" if it contains comma
          val wordUpdated = if (word contains ",") s""""$word"""" else word

          Cell(index, header, wordUpdated)
        }.toVector
      }.map(Row.apply)
    }

    rows
  }

  private def isComplexString(line: String) = !line.startsWith(",") && !line.endsWith(",")

}


