import org.bosch.common.domain.{Header, Measurement, Signal}
import org.scalatest.funsuite.AnyFunSuite
import scodec._
import scodec.bits.{ByteOrdering, ByteVector}


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

  test(":~>: test") {
    import scodec.codecs._
    val a = uint8 :: uint8
    case class Example(value1: Int, value3:Int)

    implicit val exampleCodec: Codec[Example] = (
      ("value1" | uint8) ::
        ("value3" | uint8).unit(0) :~>:
        ("value2" | uint8)
      ).as[Example]
  }

  /*
    as of current implementation, this will certainly fail, because the <<reserved>> part
    is lost during the decoding stage
    so in order to actually keep the correctness property of encoding/decoding,
    we should keep the reserved bytes and only use them when we encode the measurement back
    ^ this is just an ideea
   */

  test("measurmentToBytes") {
    //println(ByteVector.fromLong(12, 4, ByteOrdering.BigEndian))
    //println(ByteVector.fromValidHex("0x4028000000000000"))
    val testMeasurement = Measurement(12,12,12,12)
    val encodedMeasurement = Measurement.encode(testMeasurement)
    //println(encodedMeasurement)

    assert(Measurement.decode(encodedMeasurement) == testMeasurement)
  }

  test("bytesToMeasurement") {

    //indeed the 0x0000000a bytes are skipped and the measurement is created
    val testMeasurement = ByteVector.fromValidHex("0x0000000c0000000c0000000c0000000a4028000000000000")
    val decodedMeasurement = Measurement.decode(testMeasurement)
    assert(decodedMeasurement == Measurement(12,12,12,12.0))
  }

}