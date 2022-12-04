Your favorite functional CSV library: parcsv

Typical flow:
 - read CSV from different sources(file, string, map etc..)
 - choose specific columns/rows, apply some filtering etc..
 - display it to validate your intentions
 - save new updated csv somewhere

Scala code:
```scala
import com.ghurtchu.csv._

val filteredCSV = for {
  csv    <- CSV.fromFile("data/programming_languages.csv") // read CSV file
  cols   <- csv.columns("popularity", "name", "paradigm") // take only 3 columns of interest
  rows   <- csv.rows(4 to 9) // take rows within [4, 9) so rows at index 4, 5, 6, 7, 8
  newCsv <- csv.merge(cols, rows) // create new CSV file by joining cols and rows of interest
  _      <- newCsv.display // display CSV to validate your intentions
  _      <- newCsv.save("data/programming_languages_updated.csv") // save it
} yield newCsv
```



Print result:

![My Image](screenshot.png)

TODO:
 - slicing
 - merging
 - filtering
 - and much more...
