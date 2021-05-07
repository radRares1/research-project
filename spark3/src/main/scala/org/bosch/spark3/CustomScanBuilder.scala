package org.bosch.spark3

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReader, PartitionReaderFactory, Scan, ScanBuilder}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.unsafe.types.UTF8String
import org.bosch.common.domain.Record
import org.bosch.common.processing.Parser

case class CustomScanBuilder(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap) extends ScanBuilder {
  override def build(): Scan = CustomScan(schema, properties, options)
}

case class CustomScan(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap) extends Scan {
  override def readSchema(): StructType = schema

  override def toBatch: Batch = CustomBatch(schema, properties, options)

  override def description(): String = "custom_scan"
}

case class SimplePartition(val start:Int, val end:Int) extends InputPartition

case class CustomBatch(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap) extends Batch {

  val filename: String = options.get("path")
  val parser: Parser.type = Parser
  val partitionNumber: Int = parser.parseHeader(filename).signals.size


  override def planInputPartitions(): Array[InputPartition] = createPartitions() //Array(CustomInputPartition())

  def createPartitions(): Array[InputPartition] = Array(
      SimplePartition(0,partitionNumber/4),
        SimplePartition(partitionNumber/4,partitionNumber/2),
      SimplePartition(partitionNumber/2,partitionNumber*3/4),
      SimplePartition(partitionNumber*3/4,partitionNumber)
    )


  override def createReaderFactory(): PartitionReaderFactory = CustomPartitionReaderFactory(schema, filename)
}

case class CustomInputPartition() extends InputPartition {
  override def preferredLocations: Array[String] = Array.empty
}


case class CustomPartitionReaderFactory(schema: StructType, filePath: String) extends PartitionReaderFactory {
  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    CustomPartitionReader(partition.asInstanceOf[SimplePartition], schema, filePath)
  }
}

case class CustomPartitionReader(partition: SimplePartition, schema: StructType, filePath: String) extends PartitionReader[InternalRow] {
  val parser: Parser.type = Parser
  val recordIterator: Iterator[Record] = read()

  var currentIndex: Int = partition.start

  def read(): Iterator[Record] = {
    parser.parseFile(filePath).iterator
  }

  override def next(): Boolean = currentIndex <= partition.end

  override def get(): InternalRow = {

    val currentRecord = recordIterator.next()
    val fileName = UTF8String.fromString(currentRecord.filename)
    val parameterArray = ArrayData.toArrayData(Array(
      UTF8String.fromString(currentRecord.parameter.name),
      UTF8String.fromString(currentRecord.parameter.unit)))
    val timeArray = ArrayData.toArrayData(currentRecord.timeArray)
    val valueArray = ArrayData.toArrayData(currentRecord.valueArray)
    currentIndex = currentIndex+1
    InternalRow(fileName, parameterArray, timeArray, valueArray)
  }

  override def close(): Unit = ()
}