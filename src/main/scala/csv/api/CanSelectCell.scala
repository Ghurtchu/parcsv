package com.ghurtchu
package csv.api

import csv._

private[csv] trait CanSelectCell {
  def cell(rowIndex: Int, colIndex: Int): Option[Cell]
}
