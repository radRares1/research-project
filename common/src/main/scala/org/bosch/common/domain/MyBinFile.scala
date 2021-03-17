package org.bosch.common.domain

import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec}

/**
 * Represents the structure of the binary file
 *
 * @param header       the [[Header]] of the binary file
 * @param signals      the Vector of [[Signal]]s in the file
 * @param measurements the List that holds the [[Measurement]]s
 * @todo find a way to switch from list to an iterator or stream
 */
final case class MyBinFile(header: Header, signals: Vector[Signal], measurements: List[Measurement]) {

  /**
   * Encodes our binary file
   *
   * @return Successful[BitVector] which is out binary file encoded if the encoding worked, Failure otherwise
   */
  def encode: Attempt[BitVector] = MyBinFile.codec.encode(this)
}

object MyBinFile {

  implicit val codec: Codec[MyBinFile] = Header
    .codec
    .flatPrepend(header => vectorOfN(provide(header.signalNumber), Signal.codec) :: list(Measurement.codec))
    .as[MyBinFile]

  /**
   * Decodes our binary file
   *
   * @param byteVector the bytes from which the MyBinFile will be created
   * @return returns Successful[MyBinFile] if the decoding worked, Failure otherwise
   */
  def decode(byteVector: BitVector): Attempt[MyBinFile] = codec.decode(byteVector).map(_.value)
}
