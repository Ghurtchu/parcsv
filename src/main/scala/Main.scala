package com.ghurtchu

import csv._

object Main extends scala.App {

  val transformedCSV = for {
    csv  <- CSV.fromFile("data/programming_languages.csv")
    _    <- csv.display
    csv3 <- csv.filterColumns(column => column.header.value.contains("o"))
    csv4 <- csv3.dropRows(0, 6)
    _ <- csv4.display
  } yield csv3



//  val source =
//    """food,calories,protein,carbs,isHealthy
//      |apple,52,0.3,14,true
//      |egg,155,13,26,true
//      |potato,77,4.3,26,true
//      |sugar,387,0,100,false
//      |""".stripMargin
//

}
