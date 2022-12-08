package com.ghurtchu
package csv.service

import csv.api.CanWrite
import csv._

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Try

private[csv] final class CSVWriter(private val content: Content) extends CanWrite {

  override def save(fileName: String): Either[Throwable, Boolean] = for {
    saveStatus <- Try {
      Try {
        val writer = new BufferedWriter(new FileWriter(new File(fileName)))
        writer.write(content.data)

        writer.close()
      }.isSuccess
    }.toEither
  } yield saveStatus
}

private[csv] object CSVWriter {

  def apply(content: Content): CSVWriter =
    new CSVWriter(content)
}
