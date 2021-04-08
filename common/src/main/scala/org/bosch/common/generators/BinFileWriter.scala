package org.bosch.common.generators


import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp}
import fs2._
import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}
import org.bosch.common.generators.Generator.generateBinFile
import scodec.codecs.{listOfN, provide, vectorOfN}
import scodec.stream.{StreamDecoder, StreamEncoder}

import java.nio.file.{Paths, StandardOpenOption}
import scala.io.StdIn

/** Entry point of the application for generating and writing a binary file */
object BinFileWriter extends IOApp {

  val DefaultPath: String = "common/src/main/scala/org/bosch/common/out/file.txt"
  val ChunkSize: Int = 262144
  implicit val csIO: ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  /** Creates a MyBinFile and writes it to a file */
  def run(args: List[String]): IO[ExitCode] = {

    val signalNumber: Int = StdIn.readLine("Please input the number of signals: ").toInt
    val maxMeasurements: Int = StdIn.readLine("Please input the maximum number of measurements: ").toInt
    val path: String = StdIn.readLine("Please input where to save the file: ")
    val randomness: MeasurementRandomness = MeasurementRandomness(maxMeasurements)
    val binFile: MyBinFile = generateBinFile(signalNumber, randomness)
    val measurementsStream = Generator.generateStreamMeasurements(binFile.signals, randomness,chunkSize = ChunkSize)
    encodeToFile(binFile, measurementsStream, path)
  }

  /**
   * Encodes a [[MyBinFile]] and writes it to file
   *
   * @param myBinFile [[MyBinFile]] instance
   * @param path      path where the file will be stored
   */

  def encodeToFile(myBinFile: MyBinFile, measurementStream: Stream[IO, List[Measurement]], path: String = DefaultPath): IO[ExitCode] = {

    val measurementEnc = StreamEncoder.many(listOfN(provide(ChunkSize),Measurement.codec)).toPipeByte[IO]
    val fileInfoEnc = StreamEncoder
      .once(Header.codec
        .flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec)))

    //write header and signals as tuple
    Blocker[IO].use(blocker => {
      val sink = io.file.writeAll[IO](Paths.get(path),
        blocker, Seq(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND))

      Stream((myBinFile.header, myBinFile.signals))
        .through(fileInfoEnc.toPipeByte[IO])
        .through(sink)
        .compile
        .drain

    }).unsafeRunSync()

    //write the measurements
    Blocker[IO].use(blocker => {
      val sink = io.file.writeAll[IO](Paths.get(path),
        blocker, Seq(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND))

      measurementStream
        .through(measurementEnc)
        .through(sink)
        .compile
        .drain
    }).as(ExitCode.Success)

  }

  /**
   * Decodes a [[MyBinFile]] file along with its Measurements
   * @param path path to the file
   * @param chunkSize size of chunks
   * @return [[MyBinFile]] and a Stream[IO,Measurements]
   */
  def decodeFromFile(path: String, chunkSize: Int = ChunkSize): (MyBinFile, Stream[IO, List[Measurement]]) = {
    val fileDec = StreamDecoder
      .once(Header
        .codec
        .flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec))) ++
      StreamDecoder.many(listOfN(provide(ChunkSize),Measurement.codec))

    val rawData = Stream.resource(Blocker[IO]).flatMap { blocker =>
      fs2.io.file
        .readAll[IO](Paths.get(path), blocker, chunkSize)
        .through(fileDec.toPipeByte)
    }

    val fileData = rawData.take(1).compile.toList.unsafeRunSync().head.asInstanceOf[Tuple2[Header, Vector[Signal]]]
    val binFile = MyBinFile(fileData._1, fileData._2)
    val measStream = rawData.drop(1).asInstanceOf[Stream[IO, List[Measurement]]]
    (binFile, measStream)
  }
}
