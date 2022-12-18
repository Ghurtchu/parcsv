package com.ghurtchu
package csv.service

import scala.collection.immutable.ListMap

object CSVBuilder {

  import scala.util._
  import scala.io.Source.{fromFile => read}
  import com.ghurtchu.csv._

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
  private[csv] def apply(headers: Headers, rows: Rows): Either[Throwable, CSV] = {
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
