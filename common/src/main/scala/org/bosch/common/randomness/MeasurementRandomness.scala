package org.bosch.common.randomness

final case class MeasurementRandomness(maxMeasurements: Int, minTimeSec: Int, maxTimeSec: Int)

/**
 * object that holds the properties for generating the Measurements
 */
object MeasurementRandomness {
  val MaxMeasurements: Int = 10000
  val Offset: Int = 3600
  val DividentForSeconds: Int = 1000
  val UsecBound = 99999999
  val maxTimeSec: Int = (System.currentTimeMillis() / DividentForSeconds).toInt
  def minTimeSec(offset:Int):Int = maxTimeSec - offset

  def apply(maxMeasurement:Int = MaxMeasurements, offset:Int = Offset): MeasurementRandomness = {
    new MeasurementRandomness(maxMeasurement,minTimeSec(offset),maxTimeSec)
  }
}
