package com.ghurtchu

import csv._

object Main extends scala.App {

  val transformedCSV = for {
    csv             <- CSV.fromFile("data/programming_languages.csv")
    headers         <- csv.withHeaders("name", "popularity", "paradigm")
    rows            <- csv.withRows(3 to 7)
    functionalLangs <- rows.filter(_.value.contains("functional"))
    processedCSV    <- headers <+> functionalLangs // join headers and rows to get new CSV
    _               <- processedCSV.display
    _               <- processedCSV.save("data/programming_languages_updated.csv")
  } yield processedCSV

  println("--------")


  val source =
    """food,calories,protein,carbs,isHealthy
      |apple,52,0.3,14,true
      |egg,155,13,26,true
      |potato,77,4.3,26,true
      |sugar,387,0,100,false
      |""".stripMargin

  val transformedCSV2 = for {
    csv <- CSV.fromString(source)
    headers <- csv.withHeaders("isHealthy", "food", "protein")
    lowProteinFood <- csv.rows.filter { cell =>
      cell.header.value == "protein" && cell.value.toDouble <= 10
    }
    processedCSV <- headers <+> lowProteinFood
    _ <- processedCSV.display
  } yield processedCSV

}
