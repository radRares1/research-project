package org.bosch.spark3

import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types._
import org.apache.spark.sql.util.CaseInsensitiveStringMap

class CustomBinary extends TableProvider {

  val parameterSchema: StructType = new StructType().add(StructField("name", StringType)).add(StructField("unit", StringType))

  val schema: StructType =
    new StructType()
      .add(StructField("filename", StringType))
      .add("parameter", parameterSchema)
      .add(StructField("timeArray", ArrayType(LongType)))
      .add(StructField("valueArray", ArrayType(FloatType)))



  override def inferSchema(options: CaseInsensitiveStringMap): StructType = schema

  override def getTable(schema: StructType, partitioning: Array[Transform],
                        properties: java.util.Map[String, String]): Table = CustomTable(schema, properties)

  override def supportsExternalMetadata(): Boolean = true

}




