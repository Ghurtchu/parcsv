package com.ghurtchu

import csv._

object Main extends scala.App {

  val csv = for {
    file   <- CSV.fromFile("data.csv")
    sorted <- file.sortByColumn("Description", SortOrdering.Asc)
    _      <- sorted.display
  } yield ()


}
