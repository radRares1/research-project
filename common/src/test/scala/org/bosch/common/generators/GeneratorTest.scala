package org.bosch.common.generators

import org.bosch.common.domain._
import org.bosch.common.generators.Generator._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class GeneratorTest extends AnyFunSpec with Matchers {

  val SignalNumber: Int = 3
  val header: Header = Header(SignalNumber)
  val signals: Signals = generateSignals(header)
  val MaxMeasurements: Int = 10000
  val randomness: MeasurementRandomness = MeasurementRandomness(MaxMeasurements)
  val measurements: Measurements = generateMeasurements(signals, randomness)

  describe("Signals generator") {
    it("should generate the same number of signals that is provided in header") {
      signals should have size SignalNumber
    }
  }

  describe("Measurements generator") {
    it("should contain the list of measurements and not be empty") {
      measurements.size should be < SignalNumber * MaxMeasurements
    }

    it("should contain signal number distinct IDs") {
      measurements.map(_.signalId).distinct should have size SignalNumber
    }
  }

  describe("MyBinFile generator") {
    val myBinFile: MyBinFile = generateBinFile(header.signalNumber, randomness)
    it("should generate the binary file correctly") {
      MyBinFile.decode(myBinFile.encode.require).require should equal(myBinFile)
    }

    it("should contain SignalNumber signals") {
      myBinFile.signals should have size SignalNumber
    }
  }
}
