package com.ghurtchu

import csv.CSV

object Main extends scala.App {

  val csvFromFile = CSV.fromFile("data/programming_languages.csv")

  // Let's begin with simple task: display beautifully formatted CSV

  csvFromFile match {
    case Right(csv) => println(csv)
    case _ =>
  }


}
