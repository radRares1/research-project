package org.bosch.spark2.DataSourceV2Reader.DataSourceV2

import org.apache.spark.sql.functions.{array_contains, col}
import org.apache.spark.sql.{DataFrame, SparkSession}

object DataSourceReader {

  def main(args: Array[String]): Unit = {

    val ss = SparkSession
      .builder()
      .appName("DataSourceV2Reader")
      .master("local[*]")
      .getOrCreate()

    // Create a SparkContext using every core of the local machine
    val sc = ss.sparkContext

    val filePath:String = "common/src/main/scala/org/bosch/common/out/a.txt"

    val dataset = ss.read.format("org.bosch.spark2.DataSourceV2Reader.DataSourceV2").load(filePath)

    dataset.filter(array_contains(dataset("parameter"),"ch_1")).select("")

    val df = dataset.toDF()

    dataset.filter(array_contains(dataset("parameter"),"ch_1")).show()

    dataset.show()

  }

}
