package org.bosch.common.domain

import scodec.Codec
import scodec.codecs._

/**
 * class needed to represent the Measurements from our binary file
 * @param timeSec timestamp in seconds
 * @param timeUsec timestamp in microseconds
 * @param signalId id of the signal that produced the measurement
 * @param value it's value
 */
final case class Measurement(timeSec: Long, timeUsec: Long, signalId: Int, value: Double)

object Measurement {
  val ReservedSize = 4
  implicit val codec: Codec[Measurement] = (uint32 :: uint32 :: uint16 :: ignore(ReservedSize) :: double).as[Measurement]
}
