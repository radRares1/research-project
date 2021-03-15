package org.bosch.common.domain

import scodec.Codec
import scodec.codecs
import scodec.codecs.{doubleL, ignore, uint16L}

/**
 * class needed to represent the Measurements from our binary file
 * @param timeSec timestamp in seconds
 * @param timeUsec timestamp in microseconds
 * @param signalId id of the signal that produced the measurement
 * @param value it's value
 */
final case class Measurement(timeSec: Int, timeUsec: Int, signalId: Int, value: Double)

object Measurement {
  val ReservedSize = 4
  implicit val codec: Codec[Measurement] = (uint16L :: uint16L :: uint16L :: ignore(ReservedSize) :: doubleL).as[Measurement]
}
