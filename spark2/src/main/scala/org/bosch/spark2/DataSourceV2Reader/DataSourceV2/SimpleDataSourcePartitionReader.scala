package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import org.apache.spark.sql.{Encoder, Encoders, Row}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.encoders.{ExpressionEncoder, RowEncoder}
import org.apache.spark.sql.sources.v2.reader.InputPartitionReader
import org.bosch.common.domain.Record
import org.bosch.common.processing.Parser
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.v2.reader.{InputPartition, InputPartitionReader}
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.unsafe.types.UTF8String

class SimpleDataSourcePartitionReader(val filePath:String) extends InputPartitionReader[InternalRow] {

  val recordEncoder: Encoder[Record] = Encoders.product[Record]
  val recordExprEncoder: ExpressionEncoder[Record] = recordEncoder.asInstanceOf[ExpressionEncoder[Record]]

  val parser: Parser.type = Parser
  val recordIterator:Iterator[Record] = read()
  def read():Iterator[Record] = {
      parser.parseFile(filePath).iterator
  }

  def recordToSeq(record:Record):Seq[Any] = {
    Seq(UTF8String.fromString(record.filename),Array(UTF8String.fromString(record.parameter.name),UTF8String.fromString(record.parameter.unit)),record.timeArray.clone(),record.valueArray.clone())
  }

  override def next(): Boolean = recordIterator.hasNext

  //ways to create the internal row:

  //breaks
  //override def get(): InternalRow = InternalRow.fromSeq(recordToSeq(recordIterator.next()))

  //does not recognize nested case classes
  //override def get(): InternalRow = recordExprEncoder.toRow(recordIterator.next())

  override def get(): InternalRow = {

    val currentRecord = recordIterator.next()
    val fileName = UTF8String.fromString(currentRecord.filename)
    val parameterArray = ArrayData.toArrayData(Array(
      UTF8String.fromString(currentRecord.parameter.name),
      UTF8String.fromString(currentRecord.parameter.unit)))
    val timeArray = ArrayData.toArrayData(currentRecord.timeArray)
    val valueArray = ArrayData.toArrayData(currentRecord.valueArray)

    InternalRow(fileName,parameterArray, timeArray,valueArray)
  }

  override def close(): Unit = {

  }
}
