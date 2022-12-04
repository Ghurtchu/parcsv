package com.ghurtchu
package csv

import api._
import csv.element._
import csv.impl.{CSVColumnSelector, CSVContentBuilder, CSVPrettifier, CSVRowSelector, CSVWriter}

final class CSV private (override val headers: Headers, override val rows: Rows) extends CSVStructure with CSVOperations {

  private val headerPlaceMapping: Map[Header, Int] =
    headers
      .values
      .zipWithIndex
      .toMap

  override val content: Content =
    CSVContentBuilder(headers, rows)
      .content

  override def column(name: String): Option[Column] =
    CSVColumnSelector(headerPlaceMapping, headers, rows)
      .column(name)

  override def cell(rowIndex: Int, colIndex: Int): Option[Cell] = ???

  override def slice(rowRange: Range, colRange: Range): List[List[String]] = ???

  override val toString: String =
    CSVPrettifier(CSVColumnSelector.apply(headerPlaceMapping, headers, rows))
      .prettify

  override def save(filePath: String = System.currentTimeMillis().toString concat ".csv"): Boolean =
    CSVWriter(content)
      .save(filePath)

  override def row(index: Int): Option[Row] =
    CSVRowSelector(rows)
      .row(index)

  override def columns(names: String*): Either[Throwable, Columns] =
    CSVColumnSelector(headerPlaceMapping, headers, rows)
      .columns(names: _*)
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
    val headers = Headers(map.keys.map(Header.apply).toList)
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

  def fromHeadersAndRows(headers: Headers, rows: Rows): Either[Throwable, CSV] =
    Try(new CSV(headers, rows))
      .toEither

  def empty: CSV = new CSV(Headers.empty, Rows.empty)

  private def extractHeaders(csv: String): Headers = Headers {
    csv.takeWhile(_ != '\n')
      .split(",")
      .map(Header.apply)
      .toList
  }

  private def extractRows(csv: String): Rows = Rows {
    csv.dropWhile(_ != '\n')
      .tail
      .split("\n")
      .toList
      .map(_.split(",").toList.map(Cell.apply))
      .map(Row.apply)
  }

}


