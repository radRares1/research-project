package org.bosch.common.generators


import java.nio.file.{Paths, StandardOpenOption}

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp}
import fs2._
import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}
import org.bosch.common.generators.Generator.generateBinFile
import scodec.codecs.{provide, vectorOfN}
import scodec.stream.{StreamDecoder, StreamEncoder}

import scala.io.StdIn

/** Entry point of the application for generating and writing a binary file */
object BinFileWriter extends IOApp {

  val DefaultPath: String = "common/src/main/scala/org/bosch/common/out/file.txt"
  val ChunkSize: Int = 2048
  implicit val csIO: ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  /** Creates a MyBinFile and writes it to a file */
  def run(args: List[String]): IO[ExitCode] = {
    val signalNumber: Int = StdIn.readLine("Please input the number of signals: ").toInt
    val maxMeasurements: Int = StdIn.readLine("Please input the maximum number of measurements: ").toInt
    val path: String = StdIn.readLine("Please input where to save the file: ")
    val randomness: MeasurementRandomness = MeasurementRandomness(maxMeasurements)
    val binFile: MyBinFile = generateBinFile(signalNumber, randomness)
    val measurementsStream = Generator.generateStreamMeasurements(binFile.signals, randomness, chunkSize = ChunkSize)
    encodeToFile(binFile, measurementsStream, path)
  }

  /**
   * Encodes a [[MyBinFile]] and writes it to file
   *
   * @param myBinFile [[MyBinFile]] instance
   * @param path      path where the file will be stored
   */

  def encodeToFile(myBinFile: MyBinFile, measurementStream: Stream[IO, Vector[Measurement]], path: String = DefaultPath): IO[ExitCode] = {

    val measurementEnc = StreamEncoder.many(vectorOfN(provide(ChunkSize), Measurement.codec)).toPipeByte[IO]
    val fileInfoEnc = StreamEncoder
      .once(Header.codec.flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec)))

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
        blocker, Seq(StandardOpenOption.WRITE, StandardOpenOption.APPEND))

      measurementStream
        .through(measurementEnc)
        .through(sink)
        .compile
        .drain
    }).as(ExitCode.Success)
  }

  /**
   * Decodes a [[MyBinFile]] file along with its Measurements
   *
   * @param path      path to the file
   * @param chunkSize size of chunks
   * @return [[MyBinFile]] and a Stream[IO,Measurements]
   */
  def decodeFromFile(path: String, chunkSize: Int = ChunkSize): (MyBinFile, Stream[IO, Measurement]) = {

    val headerAndSignalsDecoder: StreamDecoder[(Header, Vector[Signal])] = StreamDecoder
      .once(Header.codec.flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec)))

    val measurementDecoder: StreamDecoder[Measurement] = StreamDecoder.many(Measurement.codec)

    val (header, signals) = Stream.resource(Blocker[IO]).flatMap { blocker =>
      fs2.io.file
        .readAll[IO](Paths.get(path), blocker, chunkSize)
        .through(headerAndSignalsDecoder.toPipeByte[IO])
        .head
    }
      .compile
      .toList
      .unsafeRunSync()
      .head


    val measurements: Stream[IO, Measurement] = Stream.resource(Blocker[IO]).flatMap { blocker =>
      fs2.io.file
        .readAll[IO](Paths.get(path), blocker, chunkSize)
        .drop(Header.Size + signals.length * Signal.Size)
        .through(measurementDecoder.toPipeByte[IO])
    }

    (MyBinFile(header, signals), measurements)
  }

  def decodeFromFileWithFilters(path: String, filters: Map[String, (String, String)], chunkSize: Int = ChunkSize): (MyBinFile, Stream[IO, Measurement]) = {

    val headerAndSignalsDecoder: StreamDecoder[(Header, Vector[Signal])] = StreamDecoder
      .once(Header.codec.flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec)))

    val measurementDecoder: StreamDecoder[Measurement] = StreamDecoder.many(Measurement.codec)

    val (header, signals) = Stream.resource(Blocker[IO]).flatMap { blocker =>
      fs2.io.file
        .readAll[IO](Paths.get(path), blocker, chunkSize)
        .through(headerAndSignalsDecoder.toPipeByte[IO])
        .head
    }
      .compile
      .toList
      .unsafeRunSync()
      .head

    val filteredSignals = filters.flatMap {
      case ("==", ("parameter.unit", c)) => signals.filter(_.unit == c)
      case ("==", ("parameter.name", c)) => signals.filter(_.name == c)
      case _ => Vector.empty
    }.toVector

    val measurements: Stream[IO, Measurement] = Stream.resource(Blocker[IO]).flatMap { blocker =>
      fs2.io.file
        .readAll[IO](Paths.get(path), blocker, chunkSize)
        .drop(Header.Size + signals.length * Signal.Size)
        .through(measurementDecoder.toPipeByte[IO])
    }

    val filteredMeasurements = filters.map {
      case ("==", ("valueArray", v)) => measurements.filter(e => math.abs(e.value - v.toDouble) < 1e-9)
      case (">", ("valueArray", v)) => measurements.filter(_.value > v.toDouble)
      case ("<", ("valueArray", v)) => measurements.filter(_.value < v.toDouble)
      case ("==", ("timeArray", v)) => measurements.filter(_.value == v.toLong)
      case (">", ("timeArray", v)) => measurements.filter(_.value > v.toLong)
      case ("<", ("timeArray", v)) => measurements.filter(_.value < v.toLong)
      case _ => measurements
    }.foldLeft(Stream.empty.covaryAll[IO, Measurement])(_ ++ _)

    (MyBinFile(header, filteredSignals), filteredMeasurements)
  }


  def decodeFromStream(rawStream: Stream[IO, Byte]): (MyBinFile, Stream[IO, Measurement]) = {

    val headerAndSignalsDecoder: StreamDecoder[(Header, Vector[Signal])] = StreamDecoder
      .once(Header.codec.flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec)))

    val measurementDecoder: StreamDecoder[Measurement] = StreamDecoder.many(Measurement.codec)

    val (header, signals) = rawStream
      .through(headerAndSignalsDecoder.toPipeByte[IO])
      .head
      .compile
      .toList
      .unsafeRunSync()
      .head

    val measurements: Stream[IO, Measurement] = rawStream
      .drop(Header.Size + signals.length * Signal.Size)
      .through(measurementDecoder.toPipeByte[IO])

    (MyBinFile(header, signals), measurements)
  }

  def decodeHeader(path: String, chunkSize: Int): MyBinFile = {
    val headerAndSignalsDecoder: StreamDecoder[(Header, Vector[Signal])] = StreamDecoder
      .once(Header.codec.flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec)))

    val (header, signals) = Stream.resource(Blocker[IO]).flatMap { blocker =>
      fs2.io.file
        .readAll[IO](Paths.get(path), blocker, chunkSize)
        .through(headerAndSignalsDecoder.toPipeByte[IO])
        .head
    }
      .compile
      .toList
      .unsafeRunSync()
      .head

    MyBinFile(header, signals)
  }
}
