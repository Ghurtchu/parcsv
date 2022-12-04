package com.ghurtchu
package csv.api

import csv.element.Row

private[csv] trait CanSelectRow {
  def row(index: Int): Option[Row]
}
