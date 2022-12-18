package com.ghurtchu
package csv

import csv.service._

import java.io.File

final class CSV private[csv] (private[csv] val headers: Headers, private[csv] val rows: Rows) {

  import scala.util.Try

  implicit private val self: CSV = this

  private[csv] val headerPlaceMapping: Map[Header, Int] =
    headers
      .values
      .zipWithIndex
      .toMap

  override val toString: String =
    CSVStringifier(ColumnService.apply)
      .stringify

  def raw: Content =
    ContentBuilder(headers, rows)
      .content

  def keepColumns(names: String*): Either[Throwable, CSV] =
    ColumnService
      .apply
      .keepColumns(names: _*)

  def keepColumns(range: Range): Either[Throwable, CSV] =
    ColumnService
      .apply
      .keepColumns(range)

  def dropColumns(names: String*): Either[Throwable, CSV] =
    ColumnService
      .apply
      .dropColumns(names: _*)

  def dropColumns(range: Range): Either[Throwable, CSV] =
    ColumnService
      .apply
      .dropColumns(range)

  def filterHeaders(predicate: Header => Boolean): Either[Throwable, Headers] =
    HeaderService
      .apply
      .filterHeaders(predicate)

  def save(filePath: String = System.currentTimeMillis().toString concat ".csv"): Either[Throwable, Boolean] =
    CSVWriter(raw)
      .save(filePath)

  def filterColumns(predicate: Column => Boolean): Either[Throwable, CSV] =
    ColumnService
      .apply
      .filterColumns(predicate)

  def mapHeaders(transformer: Header => String): Either[Throwable, CSV] =
    HeaderService
      .apply
      .mapHeaders(transformer)

  def transformColumns(transformer: Column => Column): Either[Throwable, CSV] =
    ColumnService
      .apply
      .transformColumns(transformer)

  def transformColumn(name: String)(transformer: Column => Column): Either[Throwable, CSV] =
    ColumnService
      .apply
      .transformColumn(name)(transformer)

  def display: Either[Throwable, Unit] =
    Try(println(this)).toEither

  def keepRows(range: Range): Either[Throwable, CSV] =
    RowService
      .apply
      .keepRows(range)

  def keepRows(indices: Int*): Either[Throwable, CSV] =
    RowService
      .apply
      .keepRows(indices: _*)

  def dropRows(range: Range): Either[Throwable, CSV] =
    RowService
      .apply
      .dropRows(range)

  def dropRows(indices: Int*): Either[Throwable, CSV] =
    RowService
      .apply
      .dropRows(indices: _*)

  def sortHeaders(ordering: SortOrdering): Either[Throwable, CSV] =
    SortService
      .apply
      .sortHeaders(ordering)

  def sortByColumn(name: String, ordering: SortOrdering): Either[Throwable, CSV] =
    SortService(name)
      .sortByColumn(ordering)

  def filterRows(predicate: Cell => Boolean): Either[Throwable, CSV] =
    RowService
      .apply
      .filterRows(predicate)

  def addRow(values: Seq[String]): Either[Throwable, CSV] =
    RowService
      .apply
      .addRow(values)

  def addColumn(name: String, values: Seq[String] = List.empty): Either[Throwable, CSV] =
    ColumnService
      .apply
      .addColumn(name, values)

  def mapRows[A](transformer: Row => A): Either[Throwable, Vector[A]] =
    RowService
      .apply
      .mapRows(transformer)

  def transformVia(pipeline: Seq[UntypedPipe]): Either[Throwable, CSV] =
    PipeService
      .apply
      .transformVia(pipeline)

}

object CSV {

  def fromString(rawCsv: String): Either[Throwable, CSV] = CSVBuilder fromString rawCsv

  def fromFile(path: String): Either[Throwable, CSV] = CSVBuilder.fromFile(path)

  def fromFile(file: File): Either[Throwable, CSV] = CSVBuilder.fromFile(file)

  def fromMap(map: Map[String, Seq[String]]): Either[Throwable, CSV] = CSVBuilder fromMap map

  private [csv] def apply(headers: Headers, rows: Rows): Either[Throwable, CSV] = CSVBuilder(headers, rows)

}


