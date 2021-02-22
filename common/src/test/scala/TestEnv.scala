import org.bosch.common.domain.Header
import org.scalatest.funsuite.AnyFunSuite
import scodec.bits.{ByteOrdering, ByteVector}
import scodec.codecs.{bytes, uint8, utf8_32}

class TestEnv extends AnyFunSuite {

  test("bytesToHeader"){
    val bytes = ByteVector.fromLong(45,8,ByteOrdering.BigEndian).drop(4)
    assert(Header.decode(bytes).signalNumber==45)
  }

  test("headerToBytes"){
    val testHeader = new Header(2)
    assert(Header.encode(testHeader).length == 4)

  }

}