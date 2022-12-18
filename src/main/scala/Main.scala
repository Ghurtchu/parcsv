package com.ghurtchu

import csv._

object Main extends scala.App {

  // raw string, map, file
  // process (add rows, remove columns, map/filter row/columns)
  // save

  val rawString =
    """name,surname,a
      |nika,gurchu,213
      |vazha,mela,101
      |gela,gela,1004
      |""".stripMargin


  val processed = for {
    csv    <- CSV fromString rawString
    sorted <- csv.sortHeaders(SortOrdering.Asc)
    _      <- sorted.display
  } yield sorted
}
