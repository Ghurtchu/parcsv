Your favorite functional CSV library: parcsv

Typical flow:
 - read CSV from different sources(file, string, map etc..)
 - choose specific columns/rows, apply some filtering etc..
 - display it to validate your intentions
 - save new updated csv somewhere

Scala code:
```scala
import com.ghurtchu.csv._

val transformedCSV = for {
  originalCSV     <- CSV.fromFile("data/programming_languages.csv") // read file
  headers         <- originalCSV.headers("name", "popularity", "paradigm") // choose headers
  rows            <- originalCSV.rows(3 to 7) // filter rows by indexes
  functionalLangs <- rows.filter(_.value.contains("functional")) // take languages which support "functional" paradigm
  processedCSV    <- headers <+> functionalLangs // join headers and rows to get new CSV
  _               <- processedCSV.display // display it to validate your intentions
  _               <- processedCSV.save("data/programming_languages_updated.csv") // save it as a file
} yield processedCSV
```



Print result:

![My Image](screenshot.png)

TODO:
 - slicing
 - merging
 - filtering
 - and much more...
