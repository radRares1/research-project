package org.bosch.spark3

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.util.ArrayData
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReader, PartitionReaderFactory, Scan, ScanBuilder, SupportsPushDownFilters}
import org.apache.spark.sql.sources.{EqualTo, Filter, GreaterThan, IsNotNull, LessThan}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.unsafe.types.UTF8String
import org.bosch.common.domain.Record
import org.bosch.common.processing.Parser

case class CustomScanBuilder(schema: StructType, properties: java.util.Map[String, String],
                             options: CaseInsensitiveStringMap) extends ScanBuilder with SupportsPushDownFilters {

  var pushedFilters1: Array[Filter] = Array[Filter]()

  override def build(): Scan = CustomScan(schema, properties, options,pushedFilters())

  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
    println("Filters " + filters.toList)
    pushedFilters1 = filters
    pushedFilters1
  }

  override def pushedFilters(): Array[Filter] = pushedFilters1
}

case class CustomScan(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap,
                      pushedFilters: Array[Filter]) extends Scan {
  override def readSchema(): StructType = schema

  override def toBatch: Batch = CustomBatch(schema, properties, options, pushedFilters)

  override def description(): String = "custom_scan"
}

case class SimplePartition(start: Int, end: Int) extends InputPartition

case class CustomBatch(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap,
                       pushedFilters: Array[Filter]) extends Batch {

  val filename: String = options.get("path")
  val parser: Parser.type = Parser
  val partitionNumber: Int = parser.parseHeader(filename).signals.size


  override def planInputPartitions(): Array[InputPartition] = createPartitions() //Array(CustomInputPartition())

  def createPartitions(): Array[InputPartition] = Array(
    SimplePartition(0, partitionNumber / 4),
    SimplePartition(partitionNumber / 4, partitionNumber / 2),
    SimplePartition(partitionNumber / 2, partitionNumber * 3 / 4),
    SimplePartition(partitionNumber * 3 / 4, partitionNumber)
  )


  override def createReaderFactory(): PartitionReaderFactory =
    CustomPartitionReaderFactory(schema, filename, pushedFilters)
}

case class CustomInputPartition() extends InputPartition {
  override def preferredLocations: Array[String] = Array.empty
}


case class CustomPartitionReaderFactory(schema: StructType, filePath: String, pushedFilters: Array[Filter]) extends PartitionReaderFactory {
  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    CustomPartitionReader(partition.asInstanceOf[SimplePartition], schema, filePath, pushedFilters)
  }
}

case class CustomPartitionReader(partition: SimplePartition, schema: StructType, filePath: String,
                                 pushedFilters: Array[Filter]) extends PartitionReader[InternalRow] {
  val parser: Parser.type = Parser
  val recordIterator: Iterator[Record] = read()

  var currentIndex: Int = partition.start

  def mapFilters(filters:Array[Filter]):Map[String,(String,String)] = {
    filters.map {
      case EqualTo("parameter.unit",value) => ("==",("parameter.unit",value.toString))
      case EqualTo("parameter.name",value) => ("==",("parameter.name",value.toString))
      case EqualTo("timeArray",value) => ("==",("timeArray",value.toString))
      case EqualTo("valueArray",value) => ("==",("valueArray",value.toString))
      case GreaterThan("timeArray", value) =>(">",("timeArray",value.toString))
      case GreaterThan("valueArray", value) =>(">",("valueArray",value.toString))
      case LessThan("timeArray", value) =>(">",("timeArray",value.toString))
      case LessThan("valueArray", value) =>(">",("valueArray",value.toString))
      case _ => ("!", ("",""))
    }.toMap
  }

  def read(): Iterator[Record] = {
    parser.parseFile(filePath,mapFilters(pushedFilters)).iterator
  }

  override def next(): Boolean = currentIndex <= partition.end

  override def get(): InternalRow = {

    val currentRecord = recordIterator.next()
    val fileName = UTF8String.fromString(currentRecord.filename)
    val timeArray = ArrayData.toArrayData(currentRecord.timeArray)
    val valueArray = ArrayData.toArrayData(currentRecord.valueArray)
    currentIndex = currentIndex + 1
    InternalRow(fileName, InternalRow(UTF8String.fromString(currentRecord.parameter.name),
      UTF8String.fromString(currentRecord.parameter.unit)), timeArray, valueArray)
  }

  override def close(): Unit = ()
}