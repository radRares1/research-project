package org.bosch.spark3

import org.apache.spark.sql.{Column, Dataset, SparkSession, functions}
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

    val ss = SparkSession
      .builder()
      .appName("DataSourceSpark3Reader")
      .master("local[*]")
      .getOrCreate()

    val total = paths.map(e => loadDataSet(ss,e.getPath.replace('\\','/'))).reduce(_ union _)

    //total.show()

    val a = total.filter(total("filename") === "b.txt")
    a.show()
    a.explain(true)

  }
}
