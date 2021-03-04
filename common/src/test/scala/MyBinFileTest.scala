import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MyBinFileTest extends AnyFunSpec with Matchers {

  describe("MyBinFile encoding") {

    val header = Header(2)
    val signals = Vector[Signal](
      Signal(1, 0, 0, "microwaves", "mic1"),
      Signal(2, 0, 12, "micromicrowaves", "micc1")
    )
    val measurements = List[Measurement](
      Measurement(1, 1, 1, 1),
      Measurement(2, 2, 2, 2)
    )

    val testBinFile = MyBinFile(header, signals, measurements)
    val encoded = testBinFile.encode

    it("should decode to the same initial value") {
      testBinFile shouldBe MyBinFile.decode(encoded.require).require
    }
  }
}
