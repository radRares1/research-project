package org.bosch.common.domain

import scodec.Codec
import scodec.codecs._

/**
 * class needed to represent the Measurements from our binary file
 * @param timeSec timestamp in seconds
 * @param timeUsec timestamp in microseconds
 * @param id id of the measurement
 * @param value it's value
 */
final case class Measurement(timeSec: Int, timeUsec: Int, id: Int, value: Double)

object Measurement {
  val ReservedSize = 4
  implicit val codec: Codec[Measurement] = (uint16 :: uint16 :: uint16 :: ignore(ReservedSize) :: double).as[Measurement]
}
