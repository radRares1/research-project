package org.bosch.spark3

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Dataset, SparkSession}
import org.bosch.common.domain.Record

import java.io.File

object DataSourceReader3 {

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def loadDataSet(session:SparkSession,path:String):Dataset[Record] = {
    import session.implicits._

    session.read
      .format("org.bosch.spark3.CustomBinary")
      .load(path)
      .as[Record]
  }

  def main(args: Array[String]): Unit = {


    val paths = getListOfFiles("common/src/main/scala/org/bosch/common/out/out")

    val session = SparkSession
      .builder()
      .appName("DataSourceSpark3Reader")
      .master("local[*]")
      .getOrCreate()

    val startTimeMillis = System.currentTimeMillis()

    loadDataSet(session,"common/src/main/scala/org/bosch/common/out/abc").show()

    val endTimeMillis = System.currentTimeMillis()
    val durationSeconds = (endTimeMillis - startTimeMillis) / 1000

    val b = paths.map(e => e.getPath.replace('\\', '/'))

    val a = session.read.format("org.bosch.spark3.CustomBinary")
      .load(b:_*)

    a.show
    a.filter(col("parameter.name") === "ch_1").show
    a.filter("filename = 'b.txt'").filter("parameter.name = 'ch_1'").show
    a.explain(true)
  }
}
