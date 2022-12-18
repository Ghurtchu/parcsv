package com.ghurtchu
package csv.service

import csv._

import scala.annotation.tailrec

class PipeService(val csv: CSV) {

  def transformVia(pipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {

    @tailrec
    def loop(currCsv: CSV, currPipeline: Seq[UntypedPipe]): Either[Throwable, CSV] = {
      if (currPipeline.isEmpty) Right(currCsv)
      else {
        val newCsv = currPipeline.head match {
          case FilterRowPipe(functions@_*)       => RowService(currCsv).filterRows(FilterRowPipe(functions: _*))
          case FilterColumnPipe(functions@_*)    => ColumnService(currCsv).filterColumns(FilterColumnPipe(functions: _*))
          case TransformColumnPipe(functions@_*) => ColumnService(currCsv).transformColumns(TransformColumnPipe(functions: _*))
        }

        newCsv match {
          case Left(err) => Left(err)
          case Right(csv) => loop(csv, currPipeline.tail)
        }
      }
    }

    loop(csv, pipeline)
  }

}

object PipeService {
  def apply(implicit csv: CSV): PipeService = new PipeService(csv)
}
