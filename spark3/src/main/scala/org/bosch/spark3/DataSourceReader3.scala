package org.bosch.spark3

import org.apache.spark.sql.{Column, SparkSession, functions}
import org.bosch.common.domain.Record

object DataSourceReader3 {

  def main(args: Array[String]): Unit = {

    val ss = SparkSession
      .builder()
      .appName("DataSourceSpark3Reader")
      .master("local[*]")
      .getOrCreate()

    import ss.implicits._

    val dataset = ss.read
      .format("org.bosch.spark3.CustomBinary")
      .load("common/src/main/scala/org/bosch/common/out/a.txt")
      .as[Record]

    dataset.show()

//    val b = dataset.filter("parameter.name = 'ch_1'")
//    b.show
//    b.explain(true)

  }
}
