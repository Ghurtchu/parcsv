package com.ghurtchu
package csv

import api._

final class CSV(filePath: String) extends CSVProtocol {

  import scala.io.Source.fromFile

  override lazy val content: Content = {
    val file = fromFile(filePath)
    val data = file.mkString.replace(" ", "")
    file.close()

    Content(data)
  }

  override val headers: List[Header] =
    content
      .data
      .takeWhile(_ != '\n')
      .split(",")
      .map(Header.apply)
      .toList

  override val rows: Rows =
    Rows(content
      .data
      .dropWhile(_ != '\n')
      .tail
      .split("\n")
      .toList
      .map(_.split(",").toList.map(Cell.apply))
      .map(Row.apply))

  override val headerPlaceMapping: Map[Header, Int] =
    headers
      .zipWithIndex
      .toMap

  override def column(name: String): Option[Column] = {
    if (!headers.map(_.value).contains(name)) None
    else {
      val header = Header(name)
      val count = headerPlaceMapping(header)
      val cells = rows.values.map(_.cells(count))

      Some(Column(header, cells))
    }
  }

  override def at(rowIndex: Int, colIndex: Int): Option[Cell] = ???

  override def slice(rowRange: Range, colRange: Range): List[List[String]] = ???

  override val toString: String = {
    val maxLengths = headers.map(_.value).map { header =>
      val col = column(header).get

      (header :: col.cells.map(_.value)).maxBy(_.length).length
    }

    val stringifiedHeaders: String = (maxLengths.zip(headers).map { tup =>
      val length = tup._1
      val header = tup._2

      header.value concat (" " * (length - header.value.length)) concat " | "
    }).reduce(_ concat _)

    val rowsStr: List[List[String]] = rows.values.map(_.cells.map(_.value))

    val stringifiedRows: String = (rowsStr.zip(List.fill(rowsStr.size)(maxLengths)).map { tup =>
      val row = tup._1
      val lengths = tup._2

      (for (i <- row.indices) yield {
        val cell = row(i)
        val length = lengths(i)

        cell + " " * (length - cell.length) + " | "
      }).reduce((a, b) => a concat b)
    }).reduce((row1, row2) => row1 concat "\n" concat row2)


    stringifiedHeaders concat "\n" concat "-" * stringifiedHeaders.length concat "\n" concat stringifiedRows
  }
}

object CSV {

  import scala.util._

  def apply(fileName: String): Either[Throwable, CSV] = Try(new CSV(fileName)).toEither

}


