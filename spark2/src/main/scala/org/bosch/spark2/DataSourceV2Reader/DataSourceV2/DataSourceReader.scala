package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import org.apache.spark.sql.{DataFrame, SparkSession}

object DataSourceReader {

  def main(args: Array[String]): Unit = {

    val startTimeMillis = System.currentTimeMillis()

    val ss = SparkSession
      .builder()
      .appName("BinaryFilesReader")
      .master("local[*]")
      .getOrCreate()

    // Create a SparkContext using every core of the local machine
    val sc = ss.sparkContext

    val filePath:String = "common/src/main/scala/org/bosch/common/out/abc"

    val dataset = ss.read.format("org.bosch.spark2.DataSourceV2Reader.DataSourceV2").load(filePath)

    dataset.show()


    val endTimeMillis = System.currentTimeMillis()
    val durationSeconds = (endTimeMillis - startTimeMillis) / 1000
    println(durationSeconds)
  }

}
