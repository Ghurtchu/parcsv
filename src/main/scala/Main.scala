package com.ghurtchu

import csv.CSV

object Main extends scala.App {

  val csv1 = for {
    originalCsv <- CSV.fromFile("data/programming_languages.csv")
    columns <- originalCsv.columns("paradigm", "name", "popularity")
    newCsv <- columns.toCSV
    _ <- newCsv.display
    _ <- newCsv.save("data/programming_languages_updated.csv")
  } yield newCsv

  val csv2 = for {
    originalCSV <- CSV.fromString {
      """name,age,occupation
        |nika,23,software developer
        |toko,21,journalist
        |gio,18,student
        |""".stripMargin
    }
    columns <- originalCSV.columns("name", "occupation")
    newCsv <- columns.toCSV
    _ <- newCsv.display
    _ <- newCsv.save("data/people_updated.csv")
  } yield newCsv

  val csv3 = for {
    originalCSV <- CSV.fromMap {
      Map(
        "band" -> ("necrophagist" :: "dying fetus" :: "brain drill" :: Nil),
        "genre" -> ("tech death" :: "brutal death" :: "chaotic tech death" :: Nil),
        "lead_singer" -> ("Muammed Suicmez" :: "John Gallagher" :: "idk" :: Nil)
      )
    }
    columns <- originalCSV.columns("band")
    newCsv <- columns.toCSV
    _ <- newCsv.display
    _ <- newCsv.save("data/bands_updated.csv")
  } yield newCsv

  val csv4 = for {
    csv <- CSV.fromFile("data/people.csv")
    rows <- csv.rows(0 to 10)
    newCsv <- rows.toCSV(csv.headers)
    _ <- newCsv.display
    _ <- newCsv.save("data/people_updated_2.csv")
  } yield newCsv

}
