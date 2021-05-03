package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.v2.reader.{InputPartition, InputPartitionReader}

class SimpleDataSourcePartition(val filePath:String) extends InputPartition[InternalRow]{

  override def createPartitionReader(): InputPartitionReader[InternalRow] = new SimpleDataSourcePartitionReader(filePath)
}