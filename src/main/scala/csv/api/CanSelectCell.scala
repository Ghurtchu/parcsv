package com.ghurtchu
package csv.api

private[csv] trait CanSelectCell {
  def cell(rowIndex: Int, colIndex: Int): Option[Cell]
}
