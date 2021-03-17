package org.bosch.common.generators

import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}
import org.bosch.common.generators.Generator.{generateMeasurements, _}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class GeneratorTest extends AnyFunSpec with Matchers {

  describe("Signals generator") {

    val header:Header = Header(3)
    val signals = generateSignals(header)
    it("should generate the vector of signals and not be empty"){
      signals.length should be > 0
    }
  }

  describe("Measurements generator") {
    val header:Header = Header(3)
    val signals:Vector[Signal] = generateSignals(header)
    val MaxMeasurements: Int = 10000
    val randomness: MeasurementRandomness = MeasurementRandomness(MaxMeasurements)
    val measurements:List[Measurement] = generateMeasurements(signals, randomness)
    it("should contain the list of measurements and not be empty"){
      measurements.length should be > 0
    }
  }

    describe("MyBinFile generator") {
      val header:Header = Header(3)
      val signals = generateSignals(header)
      val MaxMeasurements: Int = 10000
      val randomness: MeasurementRandomness = MeasurementRandomness(MaxMeasurements)
      val myBinFileTest = generateBinFile(header.signalNumber,randomness)
      it("should generate the binary file correctly"){
        MyBinFile.decode(myBinFileTest.encode.require).require should equal(myBinFileTest)
      }
    }
}
