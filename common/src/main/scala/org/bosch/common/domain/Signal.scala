package org.bosch.common.domain

import scodec.Codec
import scodec.codecs.{doubleL, fixedSizeBytes, int32L, utf8_32L}

/**
 * Signal in our binary file
 *
 * @param id     signal id of the Signal
 * @param offset offset used for encoding the measured value
 * @param factor factor used for encoding the measured value
 * @param name   named of the measured Signal
 * @param unit   unit of the measured Signal
 */
final case class Signal(id: Int, offset: Double, factor: Double, name: String, unit: String)

object Signal {
  val NameSize = 200
  val UnitSize = 55
  implicit val codec: Codec[Signal] =
    (int32L :: doubleL :: doubleL :: fixedSizeBytes(NameSize, utf8_32L) :: fixedSizeBytes(UnitSize, utf8_32L)).as[Signal]
}
