import org.bosch.common.domain.Header
import org.bosch.common.generators.Generator.{generateMeasurements, _}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class GeneratorTest extends AnyFunSpec with Matchers {

  describe("Header generator") {
    val signalNo = 3;
    val header = generateHeader(signalNo)
    it("should have the signal numbers set correctly"){
      header.signalNumber shouldBe 3
    }
  }

  describe("Signals generator") {

    val header:Header = generateHeader(3)
    val signals = generateSignals(header)
    it("should generate the vector of signals and not be empty"){
      signals.length should be > 0
    }
  }

  describe("Measurements generator") {
    val header:Header = generateHeader(3)
    val signals = generateSignals(header)
    val measurements = generateMeasurements(signals) ++ generateMeasurements(signals)
    it("should contain the list of measurements and not be empty"){
      measurements.length should be > 0
    }
  }
  // test used for creating the file, not yet a "test"
//    describe("File writer") {
//      val header:Header = generateHeader(3)
//      val signals = generateSignals(header)
//      //the list concats are a temp "workaround" for the overflowing of the measurements, still not good enough because
//      //the array with the mybinfile overflows anyway.
//      val measurements = generateMeasurements(signals)
//      writeToFile(header,signals,measurements)
//    }
}
