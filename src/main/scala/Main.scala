package com.ghurtchu

import csv._

object Main extends scala.App {

  // raw string, map, file
  // process (add rows, remove columns, map/filter row/columns)
  // save

  val rawString =
    """name,surname,a
      |nika,gurchu,a
      |vazha,mela,b
      |gela,gela,c
      |""".stripMargin


  val processed = for {
    csv    <- CSV fromString rawString
    sorted <- csv.sortByColumn("a", SortOrdering.Desc)
    _      <- sorted.display
  } yield sorted
}
