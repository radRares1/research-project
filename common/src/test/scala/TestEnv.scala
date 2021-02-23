import org.bosch.common.domain.{Header, Measurement, Signal}
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.{ByteOrdering, ByteVector}
import scodec.codecs.{bytes, uint8, utf8_32}

import java.math.BigInteger

class TestEnv extends AnyFunSuite {

  test("bytesToHeader") {
    val bytes = ByteVector.fromLong(45, 4, ByteOrdering.BigEndian)
    assert(Header.decode(bytes).signalNumber == 45)
  }

  test("headerToBytes") {
    val testHeader = new Header(2)
    assert(Header.encode(testHeader).length == 4)

  }

  test("bytesToSignal") {
    val testSignal = Signal(1, 0, 12, "test", "test/function")
    val signalAsBytes = Signal.encode(testSignal)
    assert(Signal.decode(signalAsBytes) == testSignal)
    assert(Signal.decode(ByteVector.empty) == Signal())
  }

  test("signalToBytes") {

    val testSignal = Signal(1, 0, 12, "test", "test/function")
    val encodedSignal = Signal.encode(testSignal)
    assert(Signal.decode(encodedSignal) == testSignal)
    //find a way to break the bounds, iI'm not sure this is enough
    assert(Signal.encode(Signal(Long.MaxValue+1,1,Double.PositiveInfinity,"idk","idk"))==ByteVector.empty)
  }

  /*
    as of current implementation, this will certainly fail, because the <<reserved>> part
    is lost during the decoding stage
    so in order to actually keep the correctness property of encoding/decoding,
    we should keep the reserved bytes and only use them when we encode the measurement back
    ^ this is just an ideea
   */

//  test("measurmentToBytes") {
//    val testMeasurement = Measurement(12,12,1,10.5)
//    val encodedMeasurement = Measurement.encode(testMeasurement)
//
//    //assert(Measurement.decode(encodedMeasurement) == testMeasurement)
//    assert(condition = true)
//  }

}