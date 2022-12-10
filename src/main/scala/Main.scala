package com.ghurtchu

import csv._

import javax.swing.SortOrder
import scala.collection.immutable.ListMap

object Main extends scala.App {

  val map: Map[String, List[String]] = Map(
    "name"           -> List("Nika", "Gio", "Toko"),
    "age"            -> List("23", "17", "21"),
    "career"         -> List("software", "law", "journalism"),
    "last_name"      -> List("ghurtchumelia", "ghurtchumelia", "ghurtchumelia"),
    "marital_status" -> List("single", "single", "single")
  )

  val processed = for {
    csv  <- CSV.fromMap(map)
    csv2 <- csv.sortHeaders(Ascending)
    _    <- csv2.display
  } yield csv

}
