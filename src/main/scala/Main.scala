package com.ghurtchu

import csv._

object Main extends scala.App {

  // csv as a raw string

  import com.ghurtchu.csv._

  val filterColumnPipe = FilterColumnPipe(
    col => Seq("name", "popularity", "creator").contains(col.header.value), // choose columns by names
    col => col.cells.forall(_.value.length <= 20) // keep columns with all values shorter than 20 characters
  )

  val filterRowPipe = FilterRowPipe(
    row => row.index % 2 == 1, // then take only odd-indexed rows
    row => row.isFull // then keep those which have no N/A-s
  )

  val transformColumnPipe = TransformColumnPipe(
    col => Column(col.header, col.cells.map(cell => cell.copy(value = cell.value.toUpperCase))) // make all values uppercase
  )

  // application order will be preserved from left to right
  val fullPipe = filterColumnPipe ~> filterRowPipe ~> transformColumnPipe

  val transformedCSV = for {
    csv <- CSV.fromFile("data/programming_languages.csv")
    transformed <- csv.transformVia(fullPipe)
    _ <- transformed.display
    _ <- transformed.save("data/updated.csv")
  } yield transformed

}
