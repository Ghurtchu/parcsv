package com.ghurtchu
package csv

import api._
import csv.impl._

import scala.util.Try

final class CSV private (override val headers: Headers, override val rows: Rows) extends CSVStructure with CSVOperations {

  private lazy val headerPlaceMapping: Map[Header, Int] =
    headers
      .values
      .zipWithIndex
      .toMap

  override val content: Content =
    CSVContentBuilder(headers, rows)
      .content

  def withHeaders(names: String*): Either[Throwable, Headers] = Try {
    Headers {
      names
        .map(Header.apply)
        .toList
    }
  }.toEither

  def filterHeaders(predicate: Header => Boolean): Either[Throwable, Headers] =
    headers.filter(predicate)

  override def column(name: String): Option[Column] =
    CSVColumnSelector(headerPlaceMapping, headers, rows)
      .column(name)

  override def cell(rowIndex: Int, colIndex: Int): Option[Cell] = ???

  override def slice(rowRange: Range, colRange: Range): List[List[String]] = ???

  override lazy val toString: String =
    CSVPrettifier(CSVColumnSelector.apply(headerPlaceMapping, headers, rows))
      .prettify

  override def save(filePath: String = System.currentTimeMillis().toString concat ".csv"): Either[Throwable, Boolean] =
    CSVWriter(content)
      .save(filePath)

  override def row(index: Int): Option[Row] =
    CSVRowSelector(rows)
      .row(index)

  override def columns(names: String*): Either[Throwable, Columns] =
    CSVColumnSelector(headerPlaceMapping, headers, rows)
      .columns(names: _*)

  def filterColumns(f: Column => Boolean): Either[Throwable, CSV] = {
    val columns = CSVColumnSelector(headerPlaceMapping, headers, rows)
      .columns(headers.values.map(_.value): _*)

    columns.fold(
      Left.apply,
      cols => Columns(cols.values.filter(f)).toCSV
    )
  }

  def mapHeaders(f: Header => String): Either[Throwable, CSV] = Try {
    val transformedHeaders = Headers {
      headers
        .values
        .map(Header.apply compose f)
    }

    new CSV(transformedHeaders, rows)
  }.toEither





  def display: Either[Throwable, Unit] = Try(println(this)).toEither

  override def withRows(range: Range): Either[Throwable, Rows] =
    CSVRowSelector(rows)
      .withRows(range)

  def filterRows(predicate: Cell => Boolean): Either[Throwable, Rows] =
    rows.filter(predicate)

  def rows(indices: Int*): Either[Throwable, Rows] =
    CSVRowSelector(rows)
      .rows(indices: _*)

}

object CSV {

  import scala.util._
  import scala.io.Source.{fromFile => read}

  def fromString(csvContent: String): Either[Throwable, CSV] =
    Try(new CSV(extractHeaders(csvContent), extractRows(csvContent)))
      .toEither

  def fromFile(path: String): Either[Throwable, CSV] = Try {
    val file = read(path)
    val csvContent = file.mkString

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

  // I know it's mutable, be calm, it's real world baby!
  def apply(headers: Headers, rows: Rows): Either[Throwable, CSV] = {
    val rowsBuffer = collection.mutable.ArrayBuffer.empty[Row]

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

  private def extractHeaders(csv: String): Headers = Headers {
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
          val splitted: Array[String] = rawLine.split("\"").filter(_ != ",")
          if (splitted.length > 1) {
            val processed = splitted.flatMap { each =>
              if (each.endsWith(",")) {
                val normalFirstCase = each.substring(0, each.length - 1)

                Array(normalFirstCase)
              } else if (each.startsWith(",")) {
                val normalSecondCase = each.drop(1)

                normalSecondCase.split(",")
              }
              else Array(each)
            }

            processed
          } else rawLine.split(",")
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


