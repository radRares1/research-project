package org.bosch.common.domain

import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs.{double, uint32}


final case class Measurement(timeSec: Long, timeUsec: Long, id: Long, value: Double)

object Measurement {

  implicit val codec: Codec[Measurement] = (uint32 :: uint32 :: uint32 :: double).as[Measurement]


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

    val skippedBytes: ByteVector = bytes.slice(0, 16) ++ bytes.take(8)

    val result = codec.decode(skippedBytes.bits).toOption
    result match {
      case Some(value) => value.value
      case _ => Measurement()
    }
  }

}

