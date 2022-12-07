package com.ghurtchu

import csv._

object Main extends scala.App {

  // sequential column filter
  val filterColumnPipe = FilterColumnPipe(
    col => Seq("name", "popularity", "creator").contains(col.header.value),
    col => col.cells.forall(_.value.length <= 10)
  )

  // sequential row filter
  val filterRowPipe = FilterRowPipe(
    row => row.index % 2 == 1,
    row => row.isFull
  )

  val transformColumnPipe = TransformColumnPipe(
    col => Column(col.header, col.cells.map(cell => Cell(cell.index, cell.header, cell.value.toUpperCase)))
  )

  val pipe = Seq(filterColumnPipe, filterRowPipe)

  val transformedCSV = for {
    csv         <- CSV.fromFile("data/programming_languages.csv")
    transformed <- csv.transformVia(pipe)
    _           <- transformed.display
    _           <- transformed.save("data/updated.csv")
  } yield transformed


}
