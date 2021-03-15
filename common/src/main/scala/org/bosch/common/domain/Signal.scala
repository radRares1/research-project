package org.bosch.common.domain

import scodec.Codec
import scodec.codecs.{doubleL, fixedSizeBytes, uint16L, utf8_32L}

/**
 * class needed to represent the Signals from our binary file
 * @param id signalId of the Signal
 * @param offset value needed to compute the value of the measurment
 * @param factor same as above
 * @param name the name of the Signal
 * @param unit the machine unit that recorded the given Signal
 */
final case class Signal(id: Int, offset: Double, factor: Double, name: String, unit: String)

object Signal{
  val NameSize = 200
  val UnitSize = 55
  implicit val codec: Codec[Signal] =
    (uint16L :: doubleL :: doubleL :: fixedSizeBytes(NameSize, utf8_32L) :: fixedSizeBytes(UnitSize, utf8_32L)).as[Signal]
}
