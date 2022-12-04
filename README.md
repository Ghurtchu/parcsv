Your favorite functional CSV library: parcsv

Typical flow:
 - read CSV from file
 - choose specific columns or rows
 - print it
 - save new csv somewhere

Scala code:
```scala
  val csv = for {
    csv    <- CSV.fromFile("data/programming_languages.csv") // read from file
    cols   <- csv.columns("name", "popularity", "paradigm") // take these columns
    newCsv <- cols.toCSV // create new CSV
    _      <- newCsv.display // print it
    _      <- newCsv.save("data/programming_languages_updated.csv") // save new CSV
  } yield newCsv
```



Print result:

![My Image](screenshot.png)