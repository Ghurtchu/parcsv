package com.ghurtchu
package csv.api

import csv.element.Column

private[csv] trait CanSelectColumn {
  def column(name: String): Option[Column]
}
