package com.ghurtchu
package csv.api

private[csv] trait CanSlice {
  def slice(rowRange: Range, colRange: Range): List[List[String]]
}
