package org.bosch.spark3

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReader, PartitionReaderFactory, Scan, ScanBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.unsafe.types.UTF8String
import org.bosch.common.domain.Record
import org.bosch.common.processing.Parser

case class CustomScanBuilder(schema:StructType,properties:java.util.Map[String,String],options: CaseInsensitiveStringMap) extends ScanBuilder{
  override def build(): Scan = CustomScan(schema,properties,options)
}

case class CustomScan(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap) extends Scan{
  override def readSchema(): StructType = schema

  override def toBatch: Batch = CustomBatch(schema,properties,options)

  override def description(): String = "custom_scan"
}

case class CustomBatch(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap) extends Batch {

  val filename: String = options.get("path")
  override def planInputPartitions(): Array[InputPartition] = Array(CustomInputPartition())

  override def createReaderFactory(): PartitionReaderFactory = CustomPartitionReaderFactory(schema,filename)
}

case class CustomInputPartition() extends InputPartition {
  override def preferredLocations: Array[String] = Array.empty
}


case class CustomPartitionReaderFactory(schema: StructType, filePath: String) extends PartitionReaderFactory {
  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    CustomPartitionReader(partition, schema, filePath)
  }
}

case class CustomPartitionReader(partition: InputPartition, schema: StructType, filePath: String) extends PartitionReader[InternalRow] {
  val parser: Parser.type = Parser
  val recordIterator:Iterator[Record] = read()


  def read():Iterator[Record] = {
    parser.parseFile(filePath).iterator
  }

  override def next(): Boolean = recordIterator.hasNext


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

  override def close(): Unit = ()
}