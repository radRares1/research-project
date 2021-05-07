package org.bosch.spark3

import org.apache.spark.sql.SparkSession

object DataSourceReader3 {

  def main(args: Array[String]): Unit = {

    val ss = SparkSession
      .builder()
      .appName("DataSourceSpark3Reader")
      .master("local[*]")
      .getOrCreate()

    val dataset = ss.read
      .format("org.bosch.spark3.CustomBinary")
      .load("common/src/main/scala/org/bosch/common/out/abcd")

    dataset.show()
    println(dataset.rdd.getNumPartitions)

  }

}
