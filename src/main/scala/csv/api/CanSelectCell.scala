package com.ghurtchu
package csv.api

import csv.element.Cell

private[csv] trait CanSelectCell {
  def cell(rowIndex: Int, colIndex: Int): Option[Cell]
}
