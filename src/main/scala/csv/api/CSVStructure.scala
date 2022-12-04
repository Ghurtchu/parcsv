package com.ghurtchu
package csv.api

import csv.element._

private[csv] trait CSVStructure {

  def headers: Headers

  def rows: Rows

}
