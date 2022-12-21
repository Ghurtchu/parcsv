package com.ghurtchu

import csv._

import java.io.File

object Main extends scala.App {

  val file = new File("data.csv")
  println(file.getTotalSpace)

  val csv = for {
    data   <- CSV.fromFile(new File("data.csv"))
    sorted <- data.sortByColumn("Code", SortOrdering.Asc)
    _      <- sorted.display
  } yield sorted

}
