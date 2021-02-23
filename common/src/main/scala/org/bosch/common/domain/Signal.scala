package org.bosch.common.domain

import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs.{double, uint32, utf8_32}

final case class Signal(id: Long, offset: Double, factor: Double, name: String, unit: String)

object Signal{

  implicit val codec: Codec[Signal] = (uint32 :: double :: double :: utf8_32 :: utf8_32).as[Signal]

  def apply():Signal = {
    new Signal(0,0,0,"","")
  }

  // the expected length of an encoded signal is 275,
  // our result is 360, maybe it's because the codec encodes the class instance as well as the arguments
  // need to dig deeper
  def encode(signal:Signal):ByteVector = {

    val result = codec.encode(signal).toOption
    result match {
      case Some(value) => value.bytes
      case _ => ByteVector.empty
    }

  }


  def decode(bytes:ByteVector):Signal = {

    val result = codec.decode(bytes.bits).toOption

    result match {
      case Some(value) => value.value
      case _ => Signal()
    }

  }

}