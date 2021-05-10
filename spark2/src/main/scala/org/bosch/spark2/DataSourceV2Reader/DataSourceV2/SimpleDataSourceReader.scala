package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.sources.v2.reader.{DataSourceReader, InputPartition, SupportsPushDownFilters, SupportsPushDownRequiredColumns}
import org.apache.spark.sql.types._

import java.util


class SimpleDataSourceReader(val filePath: String) extends DataSourceReader
  with SupportsPushDownRequiredColumns with SupportsPushDownFilters {

  var pushedFilters1: Array[Filter] = Array[Filter]()

  override def readSchema(): StructType = SimpleDataSourceReader.schema

  override def planInputPartitions(): util.List[InputPartition[InternalRow]] = {
    import java.util
    util.Arrays.asList(new SimpleDataSourcePartition(filePath))
  }

  override def pruneColumns(requiredSchema: StructType): Unit = ()

  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
    println("Filters " + filters.toList)
    pushedFilters1 = filters
    pushedFilters1
  }

  override def pushedFilters(): Array[Filter] = pushedFilters1
}

object SimpleDataSourceReader {
  val schema: StructType = {
    StructType(Array(StructField("filename", StringType),
      StructField("paramter", ArrayType(StringType)),
      StructField("timeArray", ArrayType(LongType)),
      StructField("valueArray", ArrayType(FloatType))))
  }
}