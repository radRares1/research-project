package org.bosch.spark2.BinaryFilesReader

import cats.effect.IO
import fs2.{Pure, Stream}
import org.apache.commons.io.FileUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.input.PortableDataStream
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.bosch.common.processing.Parser.{parseFile, parseStream}
import org.apache.commons.io.IOUtils
import org.bosch.spark2.BinaryFilesReader.Reader.processBuffer

import java.io.DataInputStream
import java.nio.{ByteBuffer, ByteOrder}
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}


object Reader {

  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)

    val ss = SparkSession
      .builder()
      .appName("BinaryFilesReader")
      .master("local[*]")
      .getOrCreate()

    // Create a SparkContext using every core of the local machine
    val sc = ss.sparkContext

    val file = sc.binaryFiles("common/src/main/scala/org/bosch/common/out/a.txt")

    import ss.implicits._

//    val n = file.flatMap(e => parseStream(Stream.emits(e._2.toArray()), e._1))
//    n.toDF().show()


    val recordRDD = file.map{ case (file, pds) =>
      val dis = pds.open()
      val bytes = Array.ofDim[Byte](4096)
      var all = scala.collection.mutable.ArrayBuffer[Stream[IO, Byte]]()
      while( dis.read(bytes) != -1) {
        all :+= Stream.fromIterator[IO](bytes.toVector.iterator)
      }

      (file,processBuffer(all))
    }.flatMap(e => parseStream(e._2, e._1))
    recordRDD.toDF().show()


    val recordRDD2 = file.map{ case (file, pds) =>
      val dis = pds.open()
      val bytes = Array.ofDim[Byte](4096)
      var all = scala.Stream[Stream[IO, Byte]]()
      while( dis.read(bytes) != -1) {
        all = scala.Stream.concat(all,scala.Stream(Stream.fromIterator[IO](bytes.toVector.iterator)))
      }

      (file,processStream(all))
    }.flatMap(e => parseStream(e._2, e._1))
    recordRDD2.toDF().show(2)

  }

  def processBuffer(buffer:ArrayBuffer[Stream[IO,Byte]]):Stream[IO,Byte] = {
    var scalaStream = scala.Stream.empty[Byte]
    buffer.foreach(e => scalaStream ++= (e.compile.toVector.unsafeRunSync().toStream))
    Stream.emits(scalaStream).covary[IO]
  }

  def processStream(stream:scala.Stream[Stream[IO,Byte]]):Stream[IO,Byte] = {
    var scalaStream = scala.Stream.empty[Byte]
    stream.foreach(e => scalaStream ++= (e.compile.toVector.unsafeRunSync().toStream))
    Stream.emits(scalaStream).covary[IO]
  }

  def fromStream[A](ss: scala.Stream[A]): Stream[IO, A] = fs2.Stream.unfold(ss) {
    case hd #:: tl => Some((hd, tl))
    case _ => None
  }

  def read(bufSize: Int, dis:DataInputStream): ByteBuffer = {

    require(bufSize >= 0, "Chunk size must be positive")

    val bytes: Array[Byte] = Array.ofDim[Byte](bufSize)

    val readBytes: Int = Try(IOUtils.read(dis, bytes)) match {

      case Failure(exception) =>
        println(s"Couldn't read chunk. Reason: $exception")
        0
      case Success(nBytes) => nBytes
    }

    ByteBuffer.wrap(bytes.take(readBytes))
  }
}
