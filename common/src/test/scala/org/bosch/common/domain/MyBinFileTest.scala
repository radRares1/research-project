package org.bosch.common.domain

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import scodec.Attempt
import scodec.bits.BitVector

class MyBinFileTest extends AnyFunSpec with Matchers {

  describe("MyBinFile encoding") {

    val header: Header = Header(2)
    val signals: Signals = Vector[Signal](
      Signal(1, 0, 0, "microwaves", "mic1"),
      Signal(2, 0, 12, "micromicrowaves", "micc1")
    )
    val measurements: Measurements = List[Measurement](
      Measurement(1, 1, 1, 1),
      Measurement(2, 2, 2, 2)
    )

    val testBinFile: MyBinFile = MyBinFile(header, signals, measurements)
    val encoded: Attempt[BitVector] = testBinFile.encode

    it("should decode to the same initial value") {
      testBinFile shouldBe MyBinFile.decode(encoded.require).require
    }
  }



}
