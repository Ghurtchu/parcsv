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

  val source = Map(
    "food" -> ("apple" :: "egg" :: "potato" :: "sugar" :: Nil),
    "calories" -> ("52" :: "155" :: "77" :: "387" :: Nil),
    "protein" -> ("0.3" :: "13" :: "4.3" :: "0" :: Nil),
    "carbs" -> ("14" :: "1.1" :: "26" :: "100" :: Nil),
    "isHealthy" -> ("true" :: "true" :: "true" :: "false" :: Nil)
  )

  val csv3 = for {
    csv <- CSV.fromMap(source)
    cols <- csv.withHeaders("food", "protein", "isHealthy")
    lowProteinRows <- csv.rows.filter { cell =>
      cell.header.value == "protein" && {
        cell.value.toDouble <= 10
      }
    }
    processedCSV <- cols <+> lowProteinRows
    _ <- processedCSV.display
  } yield processedCSV

}
