package com.ghurtchu
package csv

import api._

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Try

final class CSV(override val headers: List[Header], override val rows: Rows) extends CSVBaseProtocol with CSVSaveProtocol {

  override def content: Content = Content {
    val headersSeparated = headers.map(_.value).reduce((h1, h2) => h1 concat "," concat h2)
    val rowsList = rows.values.map { row =>
      row.cells
        .map(_.value).reduce((c1, c2) => c1 concat "," concat c2) concat "\n"
    }.reduce(_ concat _)

    headersSeparated concat "\n" concat rowsList
  }

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

  override def toString: String = {
    val maxLengthPerColumn = headers.map(_.value).map { header =>
      val col = column(header).get

      (header :: col.cells.map(_.value)).maxBy(_.length).length
    }

    val concatenatedHeaders: String = (maxLengthPerColumn.zip(headers).map { tup =>
      val length = tup._1
      val header = tup._2

      header.value concat (" " * (length - header.value.length)) concat " | "
    }).reduce(_ concat _)

    val stringifiedRows: List[List[String]] = rows.values.map(_.cells.map(_.value))

    val concatenatedRows: String = (stringifiedRows.zip(List.fill(stringifiedRows.size)(maxLengthPerColumn)).map { tup =>
      val row = tup._1
      val lengths = tup._2

      (for (i <- row.indices) yield {
        val cell = row(i)
        val length = lengths(i)

        cell + " " * (length - cell.length) + " | "
      }).reduce(_ concat _)
    }).reduce((row1, row2) => row1 concat "\n" concat row2)


    concatenatedHeaders concat "\n" concat "-" * (concatenatedHeaders.length - 1) concat "\n" concat concatenatedRows
  }

  override def save(filePath: String = System.currentTimeMillis().toString concat ".csv"): Boolean = Try {
    val file = new File(filePath)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content.data)

    bw.close()
  }.isSuccess

  protected override def headerPlaceMapping: Map[Header, Int] =
    headers
      .zipWithIndex
      .toMap
}

object CSV {

    import scala.util._
    import scala.io.Source.{fromFile => read}

  def fromString(csv: String): Either[Throwable, CSV] = Try {
    val headers = extractHeaders(csv)
    val rows = extractRows(csv)

    new CSV(headers, rows)
  }.toEither

  def fromFile(path: String): Either[Throwable, CSV] = Try {
    val file = read(path)
    val csv = file.mkString
    file.close()

    val headers = extractHeaders(csv)
    val rows = extractRows(csv)

    new CSV(headers, rows)
  }.toEither

  def fromMap(map: Map[String, List[String]]): Either[Throwable, CSV] = Try {
    val headers = map.keys.map(Header.apply).toList
    val rows = Rows {
      (map.head._2.indices by 1).map { i =>
        map.values
          .map(cols => cols(i))
          .map(Cell.apply)
          .toList
      }.map(Row.apply).toList
    }

    new CSV(headers, rows)
  }.toEither

  private def extractHeaders(csv: String): List[Header] =
    csv.takeWhile(_ != '\n')
      .split(",")
      .map(Header.apply)
      .toList

  private def extractRows(csv: String): Rows =
    Rows(csv.dropWhile(_ != '\n')
      .tail
      .split("\n")
      .toList
      .map(_.split(",").toList.map(Cell.apply))
      .map(Row.apply))

}


