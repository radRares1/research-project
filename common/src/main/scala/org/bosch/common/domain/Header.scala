package org.bosch.common.domain

import scodec.Codec
import scodec.codecs.int32L

/**
 * the beginning of the binary file
 * @param signalNumber the number of signals contained within the file
 */
final case class Header(signalNumber:Int)

object Header{
  implicit val codec: Codec[Header] = int32L.as[Header]
}
