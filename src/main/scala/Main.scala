package com.ghurtchu

import csv._

import scala.collection.mutable

object Main extends scala.App {

  val rawCsv =
    """name,age,salary,position
      |nika,23,11000,senior scala developer
      |toko,20,150,junior social media engineer
      |gio,18,0,pupil
      |laliko,20,0,student
      |""".stripMargin

  val filterColumnPipe = FilterColumnPipe(
    col => Seq("salary", "position").contains(col.header.value),
    col => col.cells.forall(_.value != "N/A")
  )

  val filterRowPipe = FilterRowPipe(
    row => row.cells.exists(cell => cell.headerValue == "salary" && cell.value.toDouble >= 10),
    row => row.isFull
  )

  val transformColumnPipe = TransformColumnPipe(
    col => Column(col.header, col.cells.map(cell => cell.copy(value = cell.value.toUpperCase)))
  )

  val pipe = filterColumnPipe ~> filterRowPipe ~> transformColumnPipe

  val program = for {
    csv  <- CSV.fromString(rawCsv)
    processed <- csv.transformVia(pipe)
    _ <- processed.display
  } yield processed


}
