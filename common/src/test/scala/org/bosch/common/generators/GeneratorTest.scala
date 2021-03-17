package org.bosch.common.generators

import org.bosch.common.domain._
import org.bosch.common.generators.Generator._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class GeneratorTest extends AnyFunSpec with Matchers {

  describe("Signals generator") {

    val header: Header = Header(3)
    val signals: Signals = generateSignals(header)
    it("should generate the vector of signals and not be empty") {
      signals should not be empty
    }
  }

  describe("Measurements generator") {
    val header: Header = Header(3)
    val signals: Signals = generateSignals(header)
    val MaxMeasurements: Int = 10000
    val randomness: MeasurementRandomness = MeasurementRandomness(MaxMeasurements)
    val measurements: Measurements = generateMeasurements(signals, randomness)
    it("should contain the list of measurements and not be empty") {
      measurements should not be empty
    }
  }

  describe("MyBinFile generator") {
    val header: Header = Header(3)
    val MaxMeasurements: Int = 10000
    val randomness: MeasurementRandomness = MeasurementRandomness(MaxMeasurements)
    val myBinFileTest: MyBinFile = generateBinFile(header.signalNumber, randomness)
    it("should generate the binary file correctly") {
      MyBinFile.decode(myBinFileTest.encode.require).require should equal(myBinFileTest)
    }
  }
}
