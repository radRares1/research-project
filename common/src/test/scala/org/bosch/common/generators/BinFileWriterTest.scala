package org.bosch.common.generators

import cats.effect.IO
import fs2.{Pure, Stream}
import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signals}
import org.bosch.common.generators.BinFileWriter.{decodeFromFile, encodeToFile}
import org.bosch.common.generators.Generator.{generateSignals, generateStreamMeasurements}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class BinFileWriterTest extends AnyFunSpec with Matchers {

  val SignalNumber: Int = 3
  val header: Header = Header(SignalNumber)
  val signals: Signals = generateSignals(header)
  val MaxMeasurements: Int = 10
  val randomness: MeasurementRandomness = MeasurementRandomness(MaxMeasurements)
  val measurements: fs2.Stream[Pure, Measurement] = generateStreamMeasurements(signals, randomness)
  val binFile:MyBinFile = MyBinFile(header,signals)
  val path:String = "common/src/main/scala/org/bosch/common/out/test.bin"
  val encodeFile:Unit = encodeToFile(binFile,measurements,path)
  val decodedFile:MyBinFile = decodeFromFile(path)._1

  describe("Writer ") {
    it("should decode the same file after encoding it") {
      binFile should equal(decodedFile)
    }
  }

}
