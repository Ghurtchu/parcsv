package com.ghurtchu
package csv

import csv.service._

import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.util.Try

trait CSVSelf { self =>
  def self: CSV
}

final class CSV private (private[csv] val headers: Headers, private[csv] val rows: Rows) {

  implicit private val self: CSV = this

  private[csv] val headerPlaceMapping: Map[Header, Int] = headers.values.zipWithIndex.toMap

  override val toString: String = CSVStringifier(ColumnService.apply).stringify

  def raw: Content = ContentBuilder(headers, rows).content

  def keepColumns(names: String*): Either[Throwable, CSV] = ColumnService.apply.keepColumns(names: _*)

  def keepColumns(range: Range): Either[Throwable, CSV] = ColumnService.apply.keepColumns(range)

  def dropColumns(names: String*): Either[Throwable, CSV] = ColumnService.apply.dropColumns(names: _*)

  def dropColumns(range: Range): Either[Throwable, CSV] = ColumnService.apply.dropColumns(range)

  def filterHeaders(predicate: Header => Boolean): Either[Throwable, Headers] = headers.filter(predicate)

  def save(filePath: String = System.currentTimeMillis().toString concat ".csv"): Either[Throwable, Boolean] = CSVWriter(raw).save(filePath)

  def filterColumns(predicate: Column => Boolean): Either[Throwable, CSV] = ColumnService.apply.filterColumns(predicate)

  def mapHeaders(transformer: Header => String): Either[Throwable, CSV] = HeaderService.apply.mapHeaders(transformer)

  def transformColumns(transformer: Column => Column): Either[Throwable, CSV] = ColumnService.apply.transformColumns(transformer)

  def transformColumn(name: String)(transformer: Column => Column): Either[Throwable, CSV] = ColumnService.apply.transformColumn(name)(transformer)

  def display: Either[Throwable, Unit] = Try(println(this)).toEither

  def keepRows(range: Range): Either[Throwable, CSV] = RowService.apply.keepRows(range)

  def keepRows(indices: Int*): Either[Throwable, CSV] = RowService.apply.keepRows(indices: _*)

  def dropRows(range: Range): Either[Throwable, CSV] = RowService.apply.dropRows(range)

  def dropRows(indices: Int*): Either[Throwable, CSV] = RowService.apply.dropRows(indices: _*)

  def sortHeaders(ordering: SortOrdering): Either[Throwable, CSV] = SortService.apply.sortHeaders(ordering)

  def sortByColumn(name: String, ordering: SortOrdering): Either[Throwable, CSV] = SortService(name).sortByColumn(ordering)

  def filterRows(predicate: Cell => Boolean): Either[Throwable, CSV] = RowService.apply.filterRows(predicate)

  def addRow(values: Seq[String]): Either[Throwable, CSV] = RowService.apply.addRow(values)

  def addColumn(name: String, values: Seq[String] = List.empty): Either[Throwable, CSV] = ColumnService.apply.addColumn(name, values)

  def mapRows[A](f: Row => A): Either[Throwable, Vector[A]] = Try(rows.mapRows(f)).toEither

  def transformVia(pipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {

    @tailrec
    def loop(csv: CSV, currPipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {
      if (currPipeline.isEmpty) Right(csv)
      else {
        val newCsv = currPipeline.head match {
          case FilterRowPipe(functions@_*)       => RowService(csv).filterRows(FilterRowPipe(functions: _*))
          case FilterColumnPipe(functions@_*)    => ColumnService(csv).filterColumns(FilterColumnPipe(functions: _*))
          case TransformColumnPipe(functions@_*) => ColumnService(csv).transformColumns(TransformColumnPipe(functions: _*))
        }

        newCsv match {
          case Left(err)  => Left(err)
          case Right(csv) => loop(csv, currPipeline.tail)
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

  private def extractHeaders(csv: String): Headers =
    Headers {
      csv
        .takeWhile(_ != '\n')
        .split(",")
        .map(Header.apply)
        .toVector
    }

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


