package org.bosch.common.domain

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import scodec.Attempt
import scodec.bits.BitVector

class MyBinFileTest extends AnyFunSpec with Matchers {

  describe("MyBinFile encoding and decoding") {

    val header: Header = Header(2)
    val signals: Signals = Vector[Signal](
      Signal(1, 0, 0, "microwaves", "mic1"),
      Signal(2, 0, 2, "micromicrowaves", "micc1")
    )

    val testBinFile: MyBinFile = MyBinFile(header, signals)
    val encoded: Attempt[BitVector] = testBinFile.encode

    it("should return the same file after encoding then decoding") {
      testBinFile shouldBe MyBinFile.decode(encoded.require).require
    }
  }



}
