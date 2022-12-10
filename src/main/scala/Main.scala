package com.ghurtchu

import csv._

object Main extends scala.App {

  val rawCsv =
    """name,age,sex,career,marital_status
      |nika,23,male,"software engineer, guitarist",single
      |anti,22,male,"guitarist, engineer",married
      |yulia,20,female,developer,N/A
      |""".stripMargin

  for {
    csv  <- CSV.fromString(rawCsv)
    csv2 <- csv.addRow(Seq("juris", "35", "male", "policeman", "married"))
    _    <- csv2.display
  } yield csv

}
