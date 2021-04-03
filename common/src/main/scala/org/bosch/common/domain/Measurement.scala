package org.bosch.common.domain

import scodec.Codec
import scodec.codecs.{doubleL, ignore, int32L}

/**
 * Measurement in our binary file
 *
 * @param timeSec  timestamp in seconds
 * @param timeUSec fractional part of a timestamp representing the microseconds
 * @param signalId id of the signal that produced the measurement
 * @param value    its value
 */
final case class Measurement(timeSec: Int, timeUSec: Int, signalId: Int, value: Double) extends Product with Serializable

object Measurement {
  val ReservedSize = 4
  implicit val codec: Codec[Measurement] = (int32L :: int32L :: int32L :: ignore(ReservedSize) :: doubleL).as[Measurement]
}
