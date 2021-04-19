package org.bosch.common.stream

import cats.effect.{Blocker, ContextShift, ExitCode, IO, IOApp}
import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}
import scodec.codecs.{listOfN, provide, vectorOfN}
import scodec.stream.{StreamDecoder, StreamEncoder}
import fs2.{Stream, io}
import org.bosch.common.generators.BinFileWriter.{ChunkSize, decodeFromFile}
import org.bosch.common.generators.{Generator, MeasurementRandomness}

import java.nio.file.{Paths, StandardOpenOption}

object Test extends App {
  implicit val csIO: ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

//    val myBinFile = MyBinFile(Header(2),Vector(Signal(1,1,1,"a","b"),Signal(2,1,1,"d","c")))
//
//    val randomness: MeasurementRandomness = MeasurementRandomness(100000)
//    val measurementsStream = Generator.generateStreamMeasurements(myBinFile.signals, randomness,4096)
//
//    val a = measurementsStream.compile.toList.unsafeRunSync().foreach(println)
//
//    val measurementEnc = StreamEncoder.many(listOfN(provide(ChunkSize),Measurement.codec))
//    val fileInfoEnc = StreamEncoder
//      .once(Header.codec
//        .flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec)))
//
//    //write header and signals as tuple
//    Blocker[IO].use(blocker => {
//      val sink = io.file.writeAll[IO](Paths.get("common/src/main/scala/org/bosch/common/out/hmu2.bin"),
//        blocker, Seq(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND))
//
//      Stream((myBinFile.header, myBinFile.signals))
//        .through(fileInfoEnc.toPipeByte[IO])
//        .through(sink)
//        .compile
//        .drain
//
//    }).unsafeRunSync()
//
//    Blocker[IO].use(blocker => {
//      val sink = io.file.writeAll[IO](Paths.get("common/src/main/scala/org/bosch/common/out/hmu2.bin"),
//        blocker, Seq(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND))
//      measurementsStream
//        .through(measurementEnc.toPipeByte[IO])
//        .through(sink)
//        .compile
//        .drain
//    }).unsafeRunSync()

//    val dec = (StreamDecoder.many(listOfN(provide(4096),Measurement.codec)))
//
//    val file = StreamDecoder
//      .once(Header.codec
//        .flatZip(header => vectorOfN(provide(header.signalNumber), Signal.codec))) ++ dec
//
//
//    val data = Stream.resource(Blocker[IO]).flatMap { blocker =>
//      fs2.io.file
//        .readAll[IO](Paths.get("common/src/main/scala/org/bosch/common/out/b.txt"), blocker, 4096)
//        .through(file.isolate(512).toPipeByte)
//    }
//
//    val readData = data.compile.toList.unsafeRunSync()
//    println("ok")
//    //val measData = data2.compile.toList.unsafeRunSync()
//    println(readData.size)
//    readData.foreach(println)

}

object Test2 extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    decodeFromFile("common/src/main/scala/org/bosch/common/out/c.txt")
    IO(ExitCode.Success)
  }
}
