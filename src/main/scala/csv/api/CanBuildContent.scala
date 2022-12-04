package com.ghurtchu
package csv.api

import csv._

private[csv] trait CanBuildContent {
  def content: Content
}
