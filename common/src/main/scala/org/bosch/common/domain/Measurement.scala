package org.bosch.common.domain

import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._


final case class Measurement(timeSec: Long, timeUsec: Long, id: Long, value: Double)


object Measurement {

  implicit val codec: Codec[Measurement] = (uint32 :: uint32 ::  uint32 :: uint32.unit(0) :~>: double).as[Measurement]


  def apply(): Measurement = {
    new Measurement(0, 0, 0, 0.0)
  }

  def encode(measurement: Measurement):ByteVector = {

    val result = codec.encode(measurement).toOption

    result match {
      case Some(value) => value.bytes
      case _ => ByteVector.empty
    }

  }

  def decode(bytes: ByteVector): Measurement = {

    val result = codec.decode(bytes.bits).toOption

    result match {
      case Some(value) => value.value
      case _ => Measurement()
    }

  }

}

