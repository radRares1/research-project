package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.plans.logical
import org.apache.spark.sql.catalyst.plans.logical.Filter
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.sources.v2.reader.{DataSourceReader, InputPartition, SupportsPushDownFilters, SupportsPushDownRequiredColumns}
import org.apache.spark.sql.types.{ArrayType, FloatType, IntegerType, LongType, StringType, StructField, StructType}

import java.util
import org.apache.spark.sql.sources.v2.reader.DataSourceReader
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownFilters
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownRequiredColumns


class SimpleDataSourceReader(val filePath:String) extends DataSourceReader
  with SupportsPushDownRequiredColumns {
  override def readSchema(): StructType = SimpleDataSourceReader.schema

  override def planInputPartitions(): util.List[InputPartition[InternalRow]] = {
    import java.util
    util.Arrays.asList(new SimpleDataSourcePartition(filePath))
  }

  override def pruneColumns(requiredSchema: StructType): Unit = ()

}

object SimpleDataSourceReader {
  val schema: StructType = {
    StructType(Array(StructField("filename", StringType),
      StructField("paramter", ArrayType(StringType)),
      StructField("timeArray", ArrayType(LongType)),
      StructField("valueArray", ArrayType(FloatType))))
  }
}