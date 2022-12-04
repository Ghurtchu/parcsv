package com.ghurtchu
package csv.impl

import csv.api.CanWrite
import csv.element.Content

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Try

private[csv] final class CSVWriter(private val content: Content) extends CanWrite {
  override def save(fileName: String): Boolean = Try {
    val writer = new BufferedWriter(new FileWriter(new File(fileName)))
    writer.write(content.data)
    writer.close()
  }.isSuccess
}

private[csv] object CSVWriter {
  def fromContent(content: Content): CanWrite = new CSVWriter(content)
}
