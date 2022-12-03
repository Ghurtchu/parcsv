package com.ghurtchu

import csv.CSV

object Main extends scala.App {

  val data = CSV("data/programming_languages.csv")

  // Let's begin with simple task: display beautifully formatted CSV
  data match {
    case Right(csv) => println(csv)
    case _ =>
  }

}
