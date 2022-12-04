package com.ghurtchu
package csv.api

import .Cell

private[csv] trait CanSelectCell {
  def cell(rowIndex: Int, colIndex: Int): Option[Cell]
}
