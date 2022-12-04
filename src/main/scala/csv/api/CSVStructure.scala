package com.ghurtchu
package csv.api

import csv.element._

private[csv] trait CSVStructure {

  def content: Content

  def headers: Headers

  def rows: Rows

}
