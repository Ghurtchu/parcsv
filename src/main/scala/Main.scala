package com.ghurtchu

import csv._

object Main extends scala.App {

  // raw string, map, file
  // process (add rows, remove columns, map/filter row/columns)
  // save

  val rawString =
    """name,surname,age,career
      |nika,ghurtchumelia,23,"software developer, financial quant"
      |john,doe,30,banker
      |helen,miller,50,musician
      |""".stripMargin

  val csvMap: Map[String, List[String]] = Map(
    "name"    -> List("nika", "john", "helen"),
    "surname" -> List("ghurtchumelia", "doe", "miller"),
    "age"     -> List("23", "30", "50"),
    "career"  -> List("software developer, financial quant", "banker", "musician")
  )

  val processed = for {
    csv  <- CSV.fromMap(csvMap)
    csv2 <- csv.filterRows { cell =>
      cell.isNumeric && cell.value.toDouble >= 25.0
    }
    csv3 <- csv2.keepColumns("surname", "career")
    csv4 <- csv3.sortHeaders(Ascending)
    csv5 <- csv4.dropColumns("surname")
    _    <- csv5.display
    _    <- csv5.save("data/grownups.csv")
  } yield ()

}
