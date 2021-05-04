package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import com.sun.tools.jdeprscan.scan.Scan
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.sources.v2.reader.{DataSourceReader, InputPartition, SupportsPushDownFilters, SupportsPushDownRequiredColumns}
import org.apache.spark.sql.types.{ArrayType, FloatType, IntegerType, LongType, StringType, StructField, StructType}

import java.util
import org.apache.spark.sql.sources.v2.reader.DataSourceReader
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownFilters
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownRequiredColumns


class SimpleDataSourceReader(val filePath: String) extends DataSourceReader
  with SupportsPushDownRequiredColumns with SupportsPushDownFilters {

  private var filters: Array[Filter] = Array.empty
  private lazy val pushedArrowFilters: Array[Filter] = {
    filters // todo filter validation & pushdown
  }
  val canPush = true

  override def readSchema(): StructType = SimpleDataSourceReader.schema

  override def planInputPartitions(): util.List[InputPartition[InternalRow]] = {
    import java.util
    util.Arrays.asList(new SimpleDataSourcePartition(filePath))
  }

  def pushFilters(filters: Array[Filter]): Array[Filter] = {
    this.filters = filters
    this.filters
  }

  override def pushedFilters: Array[Filter] = pushedArrowFilters

  //  override def pushFilters(filters: Array[Filter]): Array[Filter] = {
  //    println("Filters " + filters.toList)
  //    pushedFilters1 = filters
  //    pushedFilters1
  //  }
  //
  //  override def pushedFilters(): Array[Filter] = pushedFilters1
  override def pruneColumns(requiredSchema: StructType): Unit = ???

}

object SimpleDataSourceReader {
  val schema: StructType = {
    StructType(Array(StructField("filename", StringType),
      StructField("parameter", ArrayType(StringType)),
      StructField("timeArray", ArrayType(LongType)),
      StructField("valueArray", ArrayType(FloatType))))
  }
}