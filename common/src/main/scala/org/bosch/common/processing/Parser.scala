package org.bosch.common.processing

import cats.effect.IO
import fs2.{Chunk, Pure, Stream}
import org.bosch.common.domain.{Measurement, MyBinFile, Parameter, Record, Signal}
import org.bosch.common.generators.BinFileWriter.{decodeFromFile, decodeFromStream, decodeHeader}

object Parser {

  val DefaultPath:String = "common/src/main/scala/org/bosch/common/out/ab"
  val ChunkSize: Int = 4096
  var signalCount:Int = 0
  /**
   * Splits a Chunk into a map of Measurements by their signalId
   * @param chunk Chunk of n Measurements
   * @example {{{
   *  Chunk([Measurement(12,12,1,12.0),Measurement(21,21,2,21.0)) =>
   *  Map(1 -> Stream(Measurement(12,12,1,12.0)), 2 -> Stream(Measurement(21,21,2,21.0)))
   * }}}
   * @return map with keys the ids and values a stream of measurements
   */
  def splitChunkById(chunk: Chunk[Measurement]): Map[Int, Stream[Pure, Measurement]] =
    chunk
      .toVector
      .groupBy(_.signalId)
      .map(e => (e._1, Stream.emits(e._2)))

  /**
   * Computes the Array of Time
   * @param measurements Stream of Measurements
   * @return Array of time records
   */
  def timeArrayFromMeasurements(measurements: Stream[Pure, Measurement]): Array[Long] = {
    measurements
      .compile
      .toList
      .map(measure => {
        (measure.timeSec.toString + measure.timeUSec.toString.take(13 - measure.timeSec.toString.length)).toLong
      }).toArray
  }

  /**
   * Computes the Array of Values
   * @param signal [[Signal]] that produced the measurements
   * @param measurements Stream[Pure,Measurements]
   * @return Array of Physical values
   */
  def valueArrayFromMeasurements(signal: Signal, measurements: Stream[Pure, Measurement]): Array[Float] = {
    measurements
      .compile
      .toList
      .map(e => (signal.offset + signal.factor * e.value).toFloat)
      .toArray
  }

  /**
   * Creates a Record
   * @param fileName
   * @param signal [[Signal]] that produced the measurements
   * @param measurements Stream[Pure,Measurement]
   * @return Record
   */
  def transformToRecord(fileName: String, signal: Signal, measurements: Stream[Pure, Measurement]): Record =
    Record(
      filename = fileName,
      parameter = Parameter(signal),
      timeArray = timeArrayFromMeasurements(measurements),
      valueArray = valueArrayFromMeasurements(signal, measurements)
    )

  /**
   * Concatenates the Maps by keys, summing the values of the keys
   * @param data Unstructured collection
   * @return Map of signalId and Streams of Measurements of that id
   */
  def splitDataBySignals(data: Stream[IO, Map[Int, Stream[Pure, Measurement]]]): Map[Int, Stream[Pure, Measurement]] =
    data.compile.toList.unsafeRunSync()
      .flatMap(e => e.toSeq)
      .groupBy(_._1)
      .map { case (k, v) => k -> v.map(_._2) }
      .map(e => (e._1, e._2.reduceOption(_ ++ _).getOrElse(Stream())))

  /**
   * Parse a given file
   * @param path path to the file
   * @return List[Record]
   */
  def parseFile(path:String = DefaultPath): scala.Stream[Record] = {
    val (file,measurements) = decodeFromFile(path)
    val fileName: String = path.split("/").last
    signalCount = file.signals.size
    splitDataBySignals(
      measurements
      .chunkN(ChunkSize)
        .evalMap(e => IO(splitChunkById(e)))
    )
      .map(e => (file.signals.find(s => s.id==e._1).getOrElse(Signal(1,1,1,"1","1")),e._2))
      .map(e => transformToRecord(fileName,e._1,e._2))
      .toStream

  }

  def parseStream(rawStream: Stream[IO,Byte],path:String):scala.Stream[Record] = {
    val (file,measurements) = decodeFromStream(rawStream)
    val fileName: String = path.split("/").last
    signalCount = file.signals.size
    splitDataBySignals(
      measurements
        .chunkN(ChunkSize)
        .evalMap(e => IO(splitChunkById(e)))
    )
      .map(e => (file.signals.find(s => s.id==e._1).getOrElse(Signal(1,1,1,"1","1")),e._2))
      .map(e => transformToRecord(fileName,e._1,e._2))
      .toStream
  }

  def parseHeader(path:String):MyBinFile = {
    decodeHeader(path,ChunkSize)
  }

  def main(args: Array[String]): Unit = {
    val a = parseFile("common/src/main/scala/org/bosch/common/out/ab")
    println(a)
  }


}
