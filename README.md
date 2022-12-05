Your favorite functional CSV library: parcsv

parcsv uses `Either[Throwable, CSV]` Monad to enable functional style CSV processing.

Typical flow:
 - Read CSV from different sources (File, Raw String, Map etc..)
 - Choose specific Headers/Rows, apply some filtering etc..
 - Join filtered Rows and Headers to create new CSV
 - Display the processed CSV to validate your intentions
 - Save it

Scala code:

```scala
import com.ghurtchu.csv._

// Read -> Process -> Save

val transformedCSV = for {
  originalCSV <- CSV.fromFile("data/programming_languages.csv")
  headers <- originalCSV.withHeaders("name", "popularity", "paradigm")
  rows <- originalCSV.withRows(3 to 7)
  functionalLangs <- rows.filter(_.value.contains("functional"))
  processedCSV <- headers <+> functionalLangs // join headers and rows to get new CSV
  _  <- processedCSV.display
  _ <- processedCSV.save("data/programming_languages_updated.csv")
} yield processedCSV
```


Print result:

![My Image](screenshot.png)

Let's see another example using Map as a source

```scala
import com.ghurtchu.csv._

val source = Map(
  "food" -> ("apple" :: "egg" :: "potato" :: "sugar" :: Nil), 
  "calories" -> ("52" :: "155" :: "77" :: "387" :: Nil),
  "protein" -> ("0.3" :: "13" :: "4.3" :: "0" :: Nil),
  "carbs" -> ("14" :: "1.1" :: "26" :: "100" :: Nil),
  "isHealthy" -> ("true" :: "true" :: "true" :: "false" :: Nil)
)

val csv3 = for {
  csv <- CSV.fromMap(source)
  cols <- csv.withHeaders("food", "protein", "isHealthy")
  lowProteinRows <- csv.rows.filter { cell =>
    cell.header.value == "protein" && {
    cell.value.toDouble <= 10
    }
  }
  processedCSV <- cols <+> lowProteinRows
  _ <- processedCSV.display
} yield processedCSV
```

Print result:

![My Image](screenshot_food.png)

TODO:
 - slicing
 - merging
 - filtering
 - and much more...
