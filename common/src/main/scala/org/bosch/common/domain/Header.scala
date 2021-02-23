package org.bosch.common.domain

import org.bosch.common.domain.Signal.codec
import scodec.bits.ByteVector
import scodec.Codec
import scodec.codecs.uint32

final case class Header(signalNumber:Long)

object Header{

  implicit val codec: Codec[Header] = uint32.as[Header]

  def apply():Header = {
    new Header(0)
  }

  def encode(header:Header):ByteVector = {

    val result = codec.encode(header).toOption

    result match {
      case Some(value) => value.bytes
      case _ => ByteVector.empty
    }

  }

  def decode(bytes: ByteVector):Header = {

    val result = codec.decode(bytes.bits).toOption

    result match {
      case Some(value) => value.value
      case _ => Header()
    }

  }
}
