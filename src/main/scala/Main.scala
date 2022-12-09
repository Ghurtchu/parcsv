package com.ghurtchu

import csv._

object Main extends scala.App {

  val rawCsv =
    """name,age,sex
      |nika,23,male
      |anti,22,male
      |yulia,20,female
      |""".stripMargin

  for {
    csv  <- CSV.fromString(rawCsv)
    csv2 <- csv.addRow(Seq("juris", "35", "male"))
    _    <- csv2.display
  } yield csv

}
