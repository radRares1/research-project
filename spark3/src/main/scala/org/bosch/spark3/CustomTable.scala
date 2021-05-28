package org.bosch.spark3

import org.apache.spark.sql.connector.catalog.{SupportsRead, TableCapability}
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util

/**
 * Class that represents the structured data
 * @param sourceSchema given schema
 * @param properties a map of properties
 */
 case class CustomTable(sourceSchema:StructType, override val properties:java.util.Map[String,String]) extends SupportsRead{

  var capabilitiesS:java.util.Set[TableCapability] = new util.HashSet[TableCapability]()

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = CustomScanBuilder(sourceSchema,properties,options)

  override def name(): String = "CustomSource"

  override def schema(): StructType = sourceSchema

  /**
   * defines what this Table can do
   * @return
   */
  override def capabilities(): java.util.Set[TableCapability] = {
    if (capabilitiesS == null) {
      capabilitiesS = new java.util.HashSet()
      capabilitiesS.add(TableCapability.BATCH_READ)
    }
    else{
      capabilitiesS.add(TableCapability.BATCH_READ)
    }
    capabilitiesS
  }


}
