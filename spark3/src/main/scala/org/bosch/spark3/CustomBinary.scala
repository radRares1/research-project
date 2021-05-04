package org.bosch.spark3

import org.apache.spark.sql.connector.catalog.{SupportsRead, Table, TableCapability, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.{ArrayType, FloatType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

class CustomBinary extends TableProvider{

  val schema: StructType = {
    StructType(Array(StructField("filename", StringType),
      StructField("parameter", ArrayType(StringType)),
      StructField("timeArray", ArrayType(LongType)),
      StructField("valueArray", ArrayType(FloatType))))
  }

  override def inferSchema(options: CaseInsensitiveStringMap): StructType = null

  override def getTable(schema: StructType, partitioning: Array[Transform], properties: java.util.Map[String, String]): Table = CustomTable(this.schema,properties)

  override def supportsExternalMetadata(): Boolean = true

}




