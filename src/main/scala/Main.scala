package com.ghurtchu

import csv._

object Main extends scala.App {

  for {
    csv  <- CSV.fromFile("data.csv")
    csv2 <- csv.keepColumns("Code", "Description")
    csv3 <- csv2.mapHeaders(_.value.toLowerCase)
    csv4 <- csv3.filterRows { cell =>
      cell.belongsTo("description") &&
        cell.value.length >= 20
    }
    csv5 <- csv4.transformColumn("code")(identity)
    _    <- csv5.display
  } yield csv5


}
