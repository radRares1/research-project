package org.bosch.common.generators

import org.bosch.common.domain.{Header, Measurement, Measurements, MyBinFile, Signal, Signals}

import scala.util.Random.{nextDouble, nextInt}

/** Used to generate random signals and measurements for a binary file */
object Generator {

  /**
   * Generates vector of signals
   *
   * @param header Header object which contains the number of signals
   * @return a vector of Signals
   */
  def generateSignals(header: Header): Signals = {
    val UnitSize = 3
    (1 to header.signalNumber).map(i => Signal(
      id = i,
      offset = nextDouble,
      factor = nextDouble,
      name = "ch_" + i,
      unit = "unit_" + nextInt(UnitSize))
    ).toVector
  }

  /**
   * Generates list of Measurements
   *
   * @param signals    each measurement is produced by a signal
   * @param randomness properties used to randomize a measurement
   * @return a list of Measurements
   */
  def generateMeasurements(signals: Signals, randomness: MeasurementRandomness): Measurements = {
    val USecBound = 999999
    for {
      signal <- signals.toList
      _ <- 1 to nextInt(randomness.maxMeasurements)
    } yield Measurement(
      timeSec = nextInt(randomness.maxTimeSec),
      timeUSec = nextInt(USecBound),
      signalId = signal.id,
      value = nextDouble
    )
  }

  def generateStreamMeasurements(signals: Signals, randomness: MeasurementRandomness): Stream[Measurement] = {
    val USecBound = 999999
    for {
      _ <- (1 to nextInt(randomness.maxMeasurements)).toStream
      signal <- signals
    } yield Measurement(
      timeSec = nextInt(randomness.maxTimeSec),
      timeUSec = nextInt(USecBound),
      signalId = signal.id,
      value = nextDouble
    )
  }

  /**
   * Generates a complete binary file
   *
   * @param signalNumber          number of signals the file will contain
   * @param measurementRandomness properties used to randomize a measurement
   * @return randomized [[MyBinFile]]
   */
  def generateBinFile(signalNumber: Int, measurementRandomness: MeasurementRandomness): MyBinFile = {
    val header: Header = Header(signalNumber)
    val signals: Signals = generateSignals(header)
    MyBinFile(header, signals)
  }
}
