package com.ghurtchu
package csv.api

import csv.element.{Column, Columns}
import csv.CSV

private[csv] trait CanSelectColumns {

  def column(name: String): Option[Column]

  def columns(names: String*): Either[Throwable, Columns]

}
