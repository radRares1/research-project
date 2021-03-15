package org.bosch.common.randomness

final case class MeasurementRandomness(maxMeasurements: Int, minTimeSec: Int, maxTimeSec: Int)

/**
 * object that holds the properties for generating the Measurements
 */
object MeasurementRandomness {
  val MaxMeasurements: Int = 10000
  val Offset: Int = 3600
  val DividentForSeconds: Int = 1000
  val UsecBound = 65000
  val maxTimeSec: Int = (System.currentTimeMillis() / DividentForSeconds).toInt
  def minTimeSec:Int = maxTimeSec - Offset

  def apply(maxMeasurement:Int = MaxMeasurements, offset:Int = Offset): MeasurementRandomness = {
    MeasurementRandomness(maxMeasurement, maxTimeSec - offset, maxTimeSec)
  }
}
