package com.ghurtchu

import csv._

object Main extends scala.App {

  case class Student(name: String, age: Int)

  val rawCsv =
    """name,surname,age,career
      |john,doe,20,carpenter
      |ann,miller,25,banker
      |michael,lewis,30,singer
      |""".stripMargin

  val studentsFromCsv = for {
    csv      <- CSV.fromString(rawCsv)
    _        <- csv.display
  } yield csv

}
