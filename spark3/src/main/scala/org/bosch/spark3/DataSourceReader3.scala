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

    import ss.implicits._

    val total: Dataset[Record] = paths.map(e => loadDataSet(ss,e.getPath.replace('\\','/'))).reduceOption(_ union _).getOrElse(ss.emptyDataset[Record])


    val b = paths.map(e => e.getPath.replace('\\', '/')).reduce((a,b) => a + ";" + b)
    //println(b)
    //total.show()

    val a = ss.read.format("org.bosch.spark3.CustomBinary").option("paths", b)
      .load()//.filter("parameter.name = 'ch_1'")


    a.filter("parameter.name = 'ch_1'").show
    a.filter("filename = 'b.txt'").filter("parameter.name = 'ch_1'").show
    //a.show()
    println(a.count())
    a.explain(true)

  }
}
