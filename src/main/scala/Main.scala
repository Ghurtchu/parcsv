package com.ghurtchu

import csv.CSV

object Main extends scala.App {

  val csvFromFile = CSV.fromFile("data/programming_languages.csv")

  // Let's begin with simple task: display beautifully formatted CSV

  csvFromFile match {
    case Right(csv) => println(csv)
    case _ =>
  }

  val csvString =
    """name,age,occupation
      |nika,23,software developer
      |toko,21,journalist
      |gio,18,student
      |""".stripMargin

  CSV.fromString(csvString) match {
    case Right(value) => println(value)
    case _ =>
  }

  val csvMap =
    Map(
      "band" -> ("necrophagist" :: "dying fetus" :: Nil),
      "genre" -> ("tech death" :: "brutal death" :: Nil),
      "lead" -> ("Muammed Suicmez" :: "John Gallagher" :: Nil)
    )

  CSV.fromMap(csvMap) match {
    case Right(value) => println(value)
    case Left(_) =>
  }

}
