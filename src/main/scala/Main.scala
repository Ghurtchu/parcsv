package com.ghurtchu

import csv._

object Main extends scala.App {

  val filterColumnPipe = FilterColumnPipe(
    column => Seq("name", "popularity", "creator").contains(column.header.value), // choose: name, popularity and creator headers
    _.cells.forall(_.value.length <= 10) // for each cell of a column the value length must be less than 10 chars
  )

  val filterRowPipe = FilterRowPipe(
    _.index % 2 == 1, // choose odd-indexed rows only
    _.isFull // all cells in a row must have a value(no nulls or N/A-s)
  )

  val transformedCSV = for {
    csv  <- CSV.fromFile("data/programming_languages.csv")
    csv1 <- csv.filterColumns(filterColumnPipe)
    csv2 <- csv1.filterRows(filterRowPipe)
    _    <- csv2.display
    _    <- csv2.save("data/updatec.csv")
  } yield csv2


}
