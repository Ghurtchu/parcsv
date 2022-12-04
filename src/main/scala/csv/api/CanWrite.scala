package com.ghurtchu
package csv.api

private[csv] trait CanWrite {
  def save(fileName: String): Either[Throwable, Boolean]
}
