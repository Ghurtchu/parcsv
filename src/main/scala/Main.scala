package com.ghurtchu

import csv._

object Main extends scala.App {

  for {
    csv <- CSV.fromFile("data.csv")
    _   <- csv.display
  } yield csv

}
