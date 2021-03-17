package org.bosch.common.randomness

/**
 * Properties used to randomize a measurement
 *
 * @param maxMeasurements maximum number of measurement with the same signal id
 * @param minTimeSec      minimum time in seconds for a measurement
 * @param maxTimeSec      maximum time in seconds for a measurement
 */
final case class MeasurementRandomness(maxMeasurements: Int, minTimeSec: Int, maxTimeSec: Int)

/** Holds the properties for generating the Measurements */
object MeasurementRandomness {
  val MaxMeasurements: Int = 10000
  val Offset: Int = 3600
  val DividentForSeconds: Int = 1000
  val UsecBound = 999999
  val maxTimeSec: Int = (System.currentTimeMillis() / DividentForSeconds).toInt

  /**
   * Minimum time in seconds in a measurement
   *
   * @param offset number of seconds before max time
   * @return minimum time depending on the current time and a provided offset
   */
  def minTimeSec(offset: Int): Int = maxTimeSec - offset

  /**
   * Generates measurement randomness
   *
   * @param maxMeasurement maximum number of measurement with the same signal id
   * @param offset         number of seconds before max time
   * @return properties used to randomize a set of measurements
   */
  def apply(maxMeasurement: Int = MaxMeasurements, offset: Int = Offset): MeasurementRandomness = {
    new MeasurementRandomness(maxMeasurement, minTimeSec(offset), maxTimeSec)
  }
}
