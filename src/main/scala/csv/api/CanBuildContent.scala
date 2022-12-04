package com.ghurtchu
package csv.api

import .Content

private[csv] trait CanBuildContent {
  def content: Content
}
