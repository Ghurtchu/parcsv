package com.ghurtchu
package csv.api

private[csv] trait CSVSaveProtocol {
  def save(fileName: String): Boolean
}
