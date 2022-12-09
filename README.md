`parcsv` is a parser which treats CSV files as tabular dataframes enabling you to manipulate them fairly easily.

parcsv uses `Either[Throwable, CSV]` Monad to enable you to do functional style CSV processing.

Typical flow:
 - Read CSV from different sources (File, Raw String, Map etc..)
 - Process it
 - Save it

Scala code:

```scala
import com.ghurtchu.csv._

// csv as a raw string
val source =
  """food,calories,protein,carbs,isHealthy
    |apple,52,0.3,14,true
    |egg,155,13,26,true
    |potato,77,4.3,26,true
    |sugar,387,0,100,false
    |""".stripMargin

// let's choose columns filtered by header's value
val containsLetter: Column => Boolean = col => col.header.value.contains("o")

// only keeps those rows which have a value less than 10 under "protein" header
val lowProteinFoodFilter: Row => Boolean = row => {
  row.cells.exists { cell =>
    cell.header.value == "protein" && cell.value.toDouble <= 10
  }
}

// csv processing
val newCSV = for {
  csv  <- CSV.fromString(source) // read
  csv2 <- csv.keepColumns("food", "protein", "isHealthy") // drop "calories" and "carbs"
  csv3 <- csv2.filterRows(lowProteinFoodFilter) // take rows with "protein" value less than 10
  _    <- csv3.display // print it
  _    <- csv3.save("data/processed_food.csv") //save it
} yield csv3
```

Print result:

![My Image](screenshot_food.png)

Let's see an advanced example using `Pipes`.

`Pipe` is useful when there is a need to apply more than 1 transformation to CSV. 
They hold filter/map/reduce functions which will be applied sequentially to CSV files.

In this example we use:

- `FilterColumnPipe` - filters columns sequentially
- `FilterRowPipe` - filters rows sequentially
- `TransformColumnPipe` - transforms rows sequentially

Scala code:

```scala
import com.ghurtchu.csv._

val filterColumnPipe = FilterColumnPipe(
  col => Seq("name", "popularity", "creator").contains(col.header.value), // choose columns by names
  col => col.cells.forall(_.value.length <= 20) // keep columns with all values shorter than 20 characters
)

val filterRowPipe = FilterRowPipe(
  row => row.index % 2 == 1, // then take only odd-indexed rows
  row => row.isFull // then keep those which have no N/A-s
)

val transformColumnPipe = TransformColumnPipe(
  col => Column(col.header, col.cells.map(cell => cell.copy(value = cell.value.toUpperCase))) // make all values uppercase
)

// create bigger pipe by joining from left to right
val fullPipe = filterColumnPipe ~> filterRowPipe ~> transformColumnPipe

// processing
val transformedCSV = for {
  csv         <- CSV.fromFile("data/programming_languages.csv") // read
  transformed <- csv.transformVia(fullPipe) // apply the whole pipe
  _           <- transformed.display // print it 
  _           <- transformed.save("data/updated.csv") // save
} yield transformed

```

Print result:

![My Image](screenshot_languages.png)


TODO:
 - slicing
 - add `CSVError` hierarchy to replace `Throwable` in `Either[Throwable, CSV]`
 - and much more...
