package com.ghurtchu

import csv.CSV

object Main extends scala.App {

  val csvFromFile = CSV.fromFile("data/programming_languages.csv")

  // Let's begin with simple task: display beautifully formatted CSV

  csvFromFile match {
    case Right(csv) => {
      println(csv)
      val wasSaved = csv.save("data/languages.csv")
      println(wasSaved)
    }
    case _ =>
  }

  val csvString =
    """name,age,occupation
      |nika,23,software developer
      |toko,21,journalist
      |gio,18,student
      |""".stripMargin

  CSV.fromString(csvString) match {
    case Right(csv) => {
      println(csv)
      val wasSaved = csv.save("data/people.csv")
      println(wasSaved)
    }
    case _ =>
  }

  val csvMap =
    Map(
      "band" -> ("necrophagist" :: "dying fetus" :: "brain drill" :: Nil),
      "genre" -> ("tech death" :: "brutal death" :: "chaotic tech death" :: Nil),
      "lead_singer" -> ("Muammed Suicmez" :: "John Gallagher" :: "idk" :: Nil)
    )

  CSV.fromMap(csvMap) match {
    case Right(csv) => {
      println(csv)
      val wasSaved = csv.save("data/bands.csv")
      println(wasSaved)
    }
    case Left(_) =>
  }

}
