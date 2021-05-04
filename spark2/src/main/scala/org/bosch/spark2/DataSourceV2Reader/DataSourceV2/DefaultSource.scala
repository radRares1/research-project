package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import org.apache.spark.sql.sources.v2.reader.DataSourceReader
import org.apache.spark.sql.sources.v2.{DataSourceOptions, DataSourceV2, ReadSupport}

class DefaultSource extends DataSourceV2 with ReadSupport{

  override def createReader(options: DataSourceOptions): DataSourceReader =
    new SimpleDataSourceReader(options.get("path").get())

}
