package com.ghurtchu
package csv.api

import csv.element.Content

private[csv] trait CanBuildContent {
  def content: Content
}
