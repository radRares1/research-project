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

/**
 * Scan Builder for the Scan class
 *
 * @param schema
 * @param properties
 * @param options
 */
case class CustomScanBuilder(schema: StructType, properties: java.util.Map[String, String],
                             options: CaseInsensitiveStringMap) extends ScanBuilder with SupportsPushDownFilters {

  var pushedFilters1: Array[Filter] = Array[Filter]()

  override def build(): Scan = CustomScan(schema, properties, options, pushedFilters())

  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
    pushedFilters1 = filters
    pushedFilters1
  }

  override def pushedFilters(): Array[Filter] = pushedFilters1
}

/**
 * Class that represents the Logical Plan of the data
 *
 * @param schema        given schema
 * @param properties
 * @param options
 * @param pushedFilters pushed fitlers
 */
case class CustomScan(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap,
                      pushedFilters: Array[Filter]) extends Scan {
  override def readSchema(): StructType = schema

  override def toBatch: Batch = CustomBatch(schema, properties, options, pushedFilters)

  override def description(): String = "custom_scan"
}

case class SimplePartition(start: Int, end: Int) extends InputPartition

/**
 * Class that deals with the Batching of the data, representing the physical plan
 * It defines a factory that the executor uses to create a PartitionReader for each InputPartition
 *
 * @param schema        given schema
 * @param properties
 * @param options
 * @param pushedFilters pushed filters
 */
case class CustomBatch(schema: StructType, properties: java.util.Map[String, String], options: CaseInsensitiveStringMap,
                       pushedFilters: Array[Filter]) extends Batch {


  val partitionNumber: Int = {

    var filePath = ""
    if (options.get("path") != null) {
      filePath = options.get("path")
      Parser.parseHeader(filePath).signals.size
    }
    else {
      filePath = options.get("paths")
      val paths = filePath.split(';')

      pushedFilters match {
        case Array(_,EqualTo("filename",v),_*) => paths.filter(e => e.split("/").last == v).map(e => Parser.parseHeader(e).signals.size).sum
        case _ => paths.map(e => Parser.parseHeader(e).signals.size).sum
      }
    }
  }


  override def planInputPartitions(): Array[InputPartition] = createPartitions()


  def createPartitions(): Array[InputPartition] = Array(
    SimplePartition(0, partitionNumber / 4),
    SimplePartition(partitionNumber / 4, partitionNumber / 2),
    SimplePartition(partitionNumber / 2, partitionNumber * 3 / 4),
    SimplePartition(partitionNumber * 3 / 4, partitionNumber)
  )


  override def createReaderFactory(): PartitionReaderFactory =
    CustomPartitionReaderFactory(schema, options.asCaseSensitiveMap(), pushedFilters)
}

/**
 * Factory for the Partition Reader
 *
 * @param schema        given schema
 * @param filePath      path to the file
 * @param pushedFilters filters pushed
 */
case class CustomPartitionReaderFactory(schema: StructType, options: java.util.Map[String, String], pushedFilters: Array[Filter])
  extends PartitionReaderFactory {
  override def createReader(partition: InputPartition): PartitionReader[InternalRow] = {
    CustomPartitionReader(partition.asInstanceOf[SimplePartition], schema, options, pushedFilters)
  }
}

/**
 * class that reads a partition from the parser and apply the given filters to it
 *
 * @param partition     SimplePartition
 * @param schema        given schema
 * @param filePath      path to the file
 * @param pushedFilters filters pushed
 */
case class CustomPartitionReader(partition: SimplePartition, schema: StructType, options: java.util.Map[String, String],
                                 pushedFilters: Array[Filter]) extends PartitionReader[InternalRow] {
  val recordIterator: Array[Record] = read()

  var currentIndex: Int = partition.start

  /**
   * maps spark Filter to a Map
   *
   * @param filters array of spark Fitlers
   * @return
   */
  def mapFilters(filters: Array[Filter]): Map[String, (String, String)] = {
    filters.map {
      case EqualTo("parameter.unit", value) => ("==", ("parameter.unit", value.toString))
      case EqualTo("parameter.name", value) => ("==", ("parameter.name", value.toString))
      case EqualTo("timeArray", value) => ("==", ("timeArray", value.toString))
      case EqualTo("valueArray", value) => ("==", ("valueArray", value.toString))
      case GreaterThan("timeArray", value) => (">", ("timeArray", value.toString))
      case GreaterThan("time", value) => (">", ("time", value.toString))
      case GreaterThan("valueArray", value) => (">", ("valueArray", value.toString))
      case LessThan("timeArray", value) => (">", ("timeArray", value.toString))
      case LessThan("valueArray", value) => (">", ("valueArray", value.toString))
      case _ => ("!", ("", ""))
    }.toMap
  }

  /**
   * reads the data from the parser
   *
   * @return Iterator[Record]
   */
  def read(): Array[Record] = {

    var filePath = ""
    if (options.get("path") != null) {
      filePath = options.get("path")
      Parser.parseFile(filePath, mapFilters(pushedFilters)).toArray
    }
    else {
      filePath = options.get("paths")
      val paths = filePath.split(';')
      pushedFilters match {
        case Array(_, EqualTo("filename",value), _*) =>
          paths
            .filter(e => e.split("/").last == value)
            .map(e => Parser.parseFile(e, mapFilters(pushedFilters)).iterator)
            .reduceOption(_ ++ _).getOrElse(Iterator.empty)
            .toArray
        case _ =>
          paths
            .map(e => Parser.parseFile(e, mapFilters(pushedFilters)).iterator)
            .reduceOption(_ ++ _).getOrElse(Iterator.empty)
            .toArray
      }
    }
  }

  /**
   * checks to see if there is data left
   *
   * @return
   */
  override def next(): Boolean = currentIndex < partition.end && currentIndex < recordIterator.length

  /**
   * gets and converts the next record to an InternalRow
   *
   * @return InternalRow of Record
   */
  override def get(): InternalRow = {

    val currentRecord = recordIterator(currentIndex)
    val fileName = UTF8String.fromString(currentRecord.filename)
    val timeArray = ArrayData.toArrayData(currentRecord.timeArray)
    val valueArray = ArrayData.toArrayData(currentRecord.valueArray)
    val paramNameUTF = UTF8String.fromString(currentRecord.parameter.name)
    val paramUnitUTF = UTF8String.fromString(currentRecord.parameter.unit)
    currentIndex = currentIndex + 1

    InternalRow(fileName, InternalRow(paramNameUTF, paramUnitUTF), timeArray, valueArray)
  }

  override def close(): Unit = ()
}