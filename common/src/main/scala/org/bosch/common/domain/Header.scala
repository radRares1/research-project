package org.bosch.common.domain

import scodec.bits.ByteVector
import scodec.Codec
import scodec.codecs.uint32
final case class Header(signalNumber:Long)

object Header{

  implicit val codec: Codec[Header] = uint32.as[Header]

  def encode(header:Header):ByteVector = {
    codec.encode(header).require.bytes
  }

  def decode(bytes: ByteVector):Header = {
    codec.decode(bytes.bits).require.value
  }
}
