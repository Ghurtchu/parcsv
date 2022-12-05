package com.ghurtchu

import csv.{CSV, Rows}

object Main extends scala.App {

  val transformedCSV = for {
    originalCSV     <- CSV.fromFile("data/programming_languages.csv")
    headers         <- originalCSV.headers("name", "popularity", "paradigm") // take headers of interest
    rows            <- originalCSV.rows(5 to 15) // take rows within [0, 4) so rows at index 0, 1, 2, 3
    functionalLangs <- rows.filter(_.value.contains("functional")) // take functional languages only
    processedCSV    <- CSV(headers, functionalLangs) // create new CSV file by joining cols and rows of interest
    _               <- processedCSV.display // display CSV to validate your intentions
    _               <- processedCSV.save("data/programming_languages_updated.csv") // save it
  } yield processedCSV

  println("-" * 100)

//  val csv2 = for {
//    originalCSV <- CSV.fromString {
//      """name,age,occupation,fav_drink,fav_food
//        |nika,23,software developer,cola,burger
//        |toko,21,journalist,water,khinkali
//        |gio,18,student,wine,sausage
//        |""".stripMargin
//    }
//    columns <- originalCSV.columns("fav_food", "age", "name")
//    newCsv <- columns.toCSV
//    _ <- newCsv.display
//    _ <- newCsv.save("data/people_updated.csv")
//  } yield newCsv

  println("-" * 100)

//  val csv3 = for {
//    originalCSV <- CSV.fromMap {
//      Map(
//        "band" -> ("necrophagist" :: "dying fetus" :: "brain drill" :: Nil),
//        "genre" -> ("tech death" :: "brutal death" :: "chaotic tech death" :: Nil),
//        "lead_singer" -> ("Muammed Suicmez" :: "John Gallagher" :: "idk" :: Nil)
//      )
//    }
//    cols <- originalCSV.columns("band", "genre")
//    newCsv <- cols.toCSV
//    _ <- newCsv.display
//    _ <- newCsv.save("data/bands_updated.csv")
//  } yield newCsv

  println("-" * 100)

}
