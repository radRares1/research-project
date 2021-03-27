package org.bosch.common.generators

import cats.effect.{Blocker, ContextShift, IO}
import fs2._
import fs2.io.file
import fs2.io.file.writeAll
import org.bosch.common.domain.{Measurement, MyBinFile}
import org.bosch.common.generators.Generator.generateBinFile
import scodec.bits.BitVector
import scodec.stream.StreamEncoder

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.{Files, Paths}
import scala.io.StdIn

/** Entry point of the application for generating and writing a binary file */
object BinFileWriter {

  val DefaultPath = "common/src/main/scala/org/bosch/common/out/file.txt"

  /** Creates a MyBinFile and writes it to a file */
  def main(args: Array[String]): Unit = {
    val signalNumber: Int = StdIn.readLine("Please input the number of signals: ").toInt
    val maxMeasurements: Int = StdIn.readLine("Please input the maximum number of measurements: ").toInt
    val path: String = StdIn.readLine("Please input where to save the file: ")
    val randomness: MeasurementRandomness = MeasurementRandomness(maxMeasurements)
    val binFile: MyBinFile = generateBinFile(signalNumber, randomness)
    val stream = Generator.generateStreamMeasurements(binFile.signals, randomness)
    println(stream.length)
    val measurementsStream = Stream.unfold(stream) {
      case hd #:: tl => Some((hd, tl))
      case _ => None
    }
    writeToFile(binFile, measurementsStream, path)
  }

  /**
   * Encodes a [[MyBinFile]] and writes it to file
   *
   * @param myBinFile [[MyBinFile]] instance
   * @param path      path where the file will be stored
   */

  def writeToFile(myBinFile: MyBinFile, measurementStream: Stream[Pure, Measurement], path: String = DefaultPath): IO[Unit] = {
    implicit val ioContextShift: ContextShift[IO] =
      IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
    val encoder = StreamEncoder.many(Measurement.codec).toPipeByte[IO]
    val bytes: Array[Byte] = myBinFile.encode.require.bytes.toArray
    writeBytesToFile(Paths.get(path).toString, bytes)

    Stream.resource(Blocker[IO]).flatMap { blocker =>
      val sink = io.file.writeAll[IO](Paths.get(path),blocker)
      measurementStream
        .through(encoder)
        .through(sink)
    }.compile
      .drain
  }



  /**
   * Writes an array of bytes to a file
   *
   * @param fileOutput path where the file will be stored
   * @param bytes      array of bytes
   */
  def writeBytesToFile(fileOutput: String, bytes: Array[Byte]): Unit = {
    val fos = new BufferedOutputStream(new FileOutputStream(fileOutput))
    try {
      fos.write(bytes)
    } finally fos.close()
  }

}

