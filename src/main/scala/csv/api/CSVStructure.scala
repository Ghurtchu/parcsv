package com.ghurtchu
package csv.api

private[csv] trait CSVStructure {

  def headers: Headers

  def rows: Rows

}
