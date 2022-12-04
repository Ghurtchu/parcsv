package com.ghurtchu
package csv.api

private[csv] trait CanSelectColumn {
  def column(name: String): Option[Column]
}
