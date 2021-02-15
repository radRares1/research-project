package org.bosch
import org.scalatest.funsuite.AnyFunSuite
import scodec.codecs.{uint8, utf8_32}

class TestEnv extends AnyFunSuite {

  test("An empty Set should have size 0") {
    assert(Set.empty.isEmpty)
  }

  test("tilda"){
    val pair = utf8_32 ~ uint8
    val enc = pair.encode(("Hello", 48))
    println(enc)
    println(pair.decode(enc.require).map(e => e.value._1))
  }
}
