package com.ghurtchu

import csv._

object Main extends scala.App {

  val transformedCSV = for {
    csv          <- CSV.fromFile("data/programming_languages.csv")
    headers      <- csv.withHeaders("paradigm", "name", "creator")
    rows         <- csv.withRows(3 to 7)
    rowsFiltered <- rows.filter(_.value.contains("functional"))
    processedCSV <- headers <+> rowsFiltered // join headers and rows to create new CSV
    _            <- processedCSV.display
    _            <- processedCSV.save("data/programming_languages_updated.csv")
  } yield processedCSV

  val source =
    """food,calories,protein,carbs,isHealthy
      |apple,52,0.3,14,true
      |egg,155,13,26,true
      |potato,77,4.3,26,true
      |sugar,387,0,100,false
      |""".stripMargin

  val headerFilter: Header => Boolean = _.value.length > 5
  val rowFilter: Cell => Boolean = cell => cell.header.value == "protein" && cell.value.toDouble <= 10

  val transformedCSV2 = for {
    csv          <- CSV.fromString(source)
    headers      <- csv.filterHeaders(headerFilter)
    rows         <- csv.filterRows(rowFilter)
    processedCSV <- headers <+> rows // join headers and rows to create new CSV
    _            <- processedCSV.display
  } yield processedCSV


  val numericColumns: Column => Boolean = col => col.cells.forall(_.isNumeric)

  val transformedCSV3 = for {
    csv         <- CSV.fromString(source)
    numeric     <- csv.filterColumns(numericColumns) // take only numeric columns
    capitalized <- numeric.mapHeaders(h => h.value.capitalize) // capitalize each header
    _           <- capitalized.display
  } yield numeric

}
