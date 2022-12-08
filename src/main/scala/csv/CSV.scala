package com.ghurtchu
package csv

import csv.service._

import scala.annotation.tailrec
import scala.util.Try

final class CSV private (private val headers: Headers, private val rows: Rows) {

  private val headerPlaceMapping: Map[Header, Int] =
    headers
      .values
      .zipWithIndex
      .toMap

  def raw: Content =
    CSVContentBuilder(headers, rows)
      .content

  def keepColumns(names: String*): Either[Throwable, CSV] = {
    val newHeaders = Headers {
      names
        .map(Header.apply)
        .toList
    }

    CSV(newHeaders, rows)
  }

  def keepColumns(range: Range): Either[Throwable, CSV] = {
    val start = range.start
    val end = range.end
    val newHeaders = Headers {
      headers
        .values
        .slice(start, end + 1) // end + 1 because it uses "until" instead of "to"
    }
    val newRows = Rows {
      rows.values.map { row =>
        Row {
          row.cells.filter { cell =>
            newHeaders
              .values
              .contains(cell.header)
          }
        }
      }
    }

    CSV(newHeaders, newRows)
  }

  def dropColumns(names: String*): Either[Throwable, CSV] = {
    val newHeaders = Headers {
      headers
        .values
        .filterNot(h => names.contains(h.value))
    }
    val newRows = Rows {
      rows.values.map { row =>
        Row {
          row.cells.filter { cell =>
            newHeaders.values.contains(cell.header)
          }
        }
      }
    }

    CSV(newHeaders, newRows)
  }

  def dropColumns(range: Range): Either[Throwable, CSV] = {
    val start = range.start
    val end = range.end
    val newHeaders = Headers {
      headers
        .values
        .slice(0, start) :::
        headers
          .values
          .slice(end + 1, headers.values.size) // end + 1 because it is using "until"
    }
    val newRows = Rows {
      rows.values.map { row =>
        Row {
          row.cells.filter { cell =>
            newHeaders.values.contains(cell.header)
          }
        }
      }
    }

    CSV(newHeaders, newRows)
  }

  def filterHeaders(predicate: Header => Boolean): Either[Throwable, Headers] =
    headers.filter(predicate)

  override val toString: String =
    CSVPrettifier(CSVColumnSelector.apply(headerPlaceMapping, headers, rows))
      .prettify

  def save(filePath: String = System.currentTimeMillis().toString concat ".csv"): Either[Throwable, Boolean] =
    CSVWriter(raw)
      .save(filePath)


  def filterColumns(f: Column => Boolean): Either[Throwable, CSV] =
    CSVColumnSelector(headerPlaceMapping, headers, rows)
      .columns(headers.values.map(_.value): _*)
      .fold(Left.apply, columns => Columns(columns.values.filter(f)).toCSV)

  def filterColumns(csv: CSV, filterColumnPipe: FilterColumnPipe): Either[Throwable, CSV] = {
    val columns = CSVColumnSelector(csv.headerPlaceMapping, csv.headers, csv.rows)
      .columns(headers.values.map(_.value): _*)

    @tailrec
    def loop(currentCSV: CSV, currentColumns: Columns, pipe: FilterColumnPipe): Either[Throwable, CSV] = {
      if (pipe.functions.isEmpty) Right(currentCSV)
      else {
        val predicate = pipe.functions.head
        val newColumns = Columns(currentColumns.values.filter(predicate))

        newColumns.toCSV match {
          case Left(err) => Left(err)
          case Right(csv) => loop(csv, newColumns, FilterColumnPipe(pipe.functions.tail: _*))
        }
      }
    }

    columns match {
      case Left(err) => Left(err)
      case Right(cols) => loop(csv, cols, filterColumnPipe)
    }
  }

  def mapHeaders(f: Header => String): Either[Throwable, CSV] = {
    val transformedHeaders = Headers {
      headers
        .values
        .map(f andThen Header.apply)
    }

    val newRows = Rows {
      rows.values.map { row =>
        Row {
          row.cells.zip(transformedHeaders.values).map { case (cell, header) =>
            cell.copy(header = header)
          }
        }
      }
    }


    CSV(transformedHeaders, newRows)
  }

  def transformColumns(f: Column => Column): Either[Throwable, CSV] =
    CSVColumnSelector(headerPlaceMapping, headers, rows)
      .columns(headers.values.map(_.value): _*)
      .fold(Left.apply, col => Columns(col.values.map(f)).toCSV)

  def transformColumn(name: String)(f: Column => Column): Either[Throwable, CSV] =
    CSVColumnSelector(headerPlaceMapping, headers, rows)
      .columns(headers.values.map(_.value): _*)
      .fold(Left.apply, cols => {
        Columns {
          cols
            .values
            .map { col =>
              if (col.header.value == name) {
                val newCol = f(col)

                newCol.copy(cells = newCol.cells.map(_.copy(header = newCol.header)))
              }
              else col
            }
        }.toCSV
      })


  private def transformColumns(csv: CSV, transformColumnPipe: TransformColumnPipe): Either[Throwable, CSV] = {
    val columns = CSVColumnSelector(csv.headerPlaceMapping, csv.headers, csv.rows)
      .columns(headers.values.map(_.value): _*)

    @tailrec
    def loop(currentCSV: CSV, currentColumns: Columns, pipe: TransformColumnPipe): Either[Throwable, CSV] = {
      if (pipe.functions.isEmpty) Right(currentCSV)
      else {
        val transformer = pipe.functions.head
        val newCols = Columns(currentColumns.values.map(transformer))

        newCols.toCSV match {
          case Left(err) => Left(err)
          case Right(csv) => loop(csv, newCols, TransformColumnPipe(pipe.functions.tail: _*))
        }
      }
    }

    columns match {
      case Left(err) => Left(err)
      case Right(cols) => loop(csv, cols, transformColumnPipe)
    }
  }

  def display: Either[Throwable, Unit] =
    Try(println(this)).toEither

  def keepRows(range: Range): Either[Throwable, CSV] =
    CSVRowSelector(rows)
      .withRows(range)
      .fold(Left.apply, CSV(headers, _))

  def keepRows(indices: Int*): Either[Throwable, CSV] =
    CSVRowSelector(rows)
      .rows(indices: _*)
      .fold(Left.apply, CSV(headers, _))

  def dropRows(range: Range): Either[Throwable, CSV] = {
    val newRows = Rows {
      rows.values.filter { row =>
        val index = row.cells.head.index

        index < range.start || index > range.end
      }
    }

    CSV(headers, newRows)
  }

  def dropRows(indices: Int*): Either[Throwable, CSV] = {
    val newRows = Rows {
      rows.values
        .zipWithIndex.filter { case (_, ind) =>

        !(indices contains ind)
      }.map(_._1)
    }

    CSV(headers, newRows)
  }

  def filterRows(predicate: Cell => Boolean): Either[Throwable, CSV] = {
    val filteredRows = Rows {
      rows
        .values
        .filter(_.cells.exists(predicate))
    }

    CSV(headers, filteredRows)
  }

  def filterRows(csv: CSV, filterRowPipe: FilterRowPipe): Either[Throwable, CSV] = {

    @tailrec
    def loop(currentCSV: CSV, currentRows: Rows, currentFilterRowPipe: FilterRowPipe): Either[Throwable, CSV] = {
      if (currentFilterRowPipe.functions.isEmpty) Right(currentCSV)
      else {
        val predicate = currentFilterRowPipe.functions.head
        val newRows = Rows(currentRows.values.filter(predicate))
        val newHeaders = Headers(newRows.values.head.cells.map(_.header))

        CSV(newHeaders, newRows) match {
          case Right(csv) => loop(csv, newRows, FilterRowPipe(currentFilterRowPipe.functions.tail: _*))
          case Left(err)  => Left(err)
        }
      }
    }

    loop(csv, csv.rows, filterRowPipe)
  }

  def transformVia(pipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {

    @tailrec
    def loop(csv: CSV, currPipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {
      if (currPipeline.isEmpty) Right(csv)
      else {
        currPipeline.head match {
          case TransformColumnPipe(functions@_*) => {
            val newCsv = transformColumns(csv, TransformColumnPipe(functions: _*))

            newCsv match {
              case Left(err) => Left(err)
              case Right(csv) => loop(csv, currPipeline.tail)
            }
          }

          case FilterColumnPipe(functions@_*) => {
            val newCSV = filterColumns(csv, FilterColumnPipe(functions: _*))

            newCSV match {
              case Left(value) => Left(value)
              case Right(csv) => loop(csv, currPipeline.tail)
            }

          }

          case FilterRowPipe(functions@_*) => {
            val newCSV = filterRows(csv, FilterRowPipe(functions: _*))

            newCSV match {
              case Left(value) => Left(value)
              case Right(csv) => loop(csv, currPipeline.tail)
            }
          }
        }
      }
    }

    loop(this, pipeline)
  }

}

object CSV {

  import scala.util._
  import scala.io.Source.{fromFile => read}

  def fromString(csvContent: String): Either[Throwable, CSV] =
    Try(new CSV(extractHeaders(csvContent), extractRows(csvContent)))
      .toEither

  def fromFile(path: String): Either[Throwable, CSV] = Try {
    val file = read(path)
    val csvContent = file.mkString.replace("\r", "")

    file.close()

    val headers = extractHeaders(csvContent)
    val rows = extractRows(csvContent)

    new CSV(headers, rows)
  }.toEither

  def fromMap(map: Map[String, List[String]]): Either[Throwable, CSV] = Try {
    val headers = Headers(map.keys.map(Header.apply).toList)

    val stringRows = (map.head._2.indices by 1).map { i =>
      map
        .values
        .map(cols => cols(i))
    }

    val rows = Rows {
      (for (i <- stringRows.indices) yield {
        val stringCells = stringRows(i)

        stringCells.zip(headers.values).map { case (strCell, header) =>

          Cell(i, header, strCell)
        }.toList
      }).map(Row.apply).toList
    }

    new CSV(headers, rows)
  }.toEither

  // I know it's mutable, be calm purist, it's a local function, it's real world baby!
  def apply(headers: Headers, rows: Rows): Either[Throwable, CSV] = {
    val rowsBuffer = collection.mutable.Stack.empty[Row]
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

      rowsBuffer.append(Row(sortedCells.toList))
    }

    Try(new CSV(headers, Rows(rowsBuffer.toList)))
      .toEither
  }

  private def extractHeaders(csv: String): Headers =
    Headers {
      csv.takeWhile(_ != '\n')
        .split(",")
        .map(Header.apply)
        .toList
    }

  private def extractRows(csv: String): Rows = {
    val headers: Array[Header] = csv.takeWhile(_ != '\n')
      .split(",")
      .map(Header.apply)
    val rows = Rows {
      csv.dropWhile(_ != '\n')
        .tail
        .split("\n")
        .toList
        .map { rawLine =>
          val splitted: Array[String] = rawLine.split("\"").filter(_.trim != "")
          // if contains complex strings
          if (splitted.length > 1) {
            val processed = splitted.flatMap { line =>
              if (!line.startsWith(",") && !line.endsWith(",")) {
                Array(line)
              } else {
                line.split(",").filter(s => s.trim != "," && s.trim != "")
              }
            }

            processed
          } else rawLine.split(",").filter(_.trim != "")
        }.zipWithIndex.map { case (line, index) =>
        line.zip(headers).map { case (word, header) =>
          // put string in "quotes" if it contains comma
          val wordUpdated = if (word contains ",") s""""$word"""" else word

          Cell(index, header, wordUpdated)
        }.toList
      }.map(Row.apply)
    }

    rows
  }
}


