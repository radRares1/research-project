package org.bosch.common.generators

import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}

import scala.util.Random.{nextDouble, nextInt}

/** Used to generate random signals and measurements for a binary file */
object Generator {
  val RandomIntBound = 10
  val UnitSize = 3
  val DefaultPath = "common/src/main/scala/org/bosch/common/out/file.txt"

  /**
   * Generates vector of signals
   *
   * @param header Header object which contains the number of signals
   * @return a vector of Signals
   */
  def generateSignals(header: Header): Vector[Signal] =
    (1 to header.signalNumber).map(i =>
      Signal(
        id = i,
        offset = nextDouble * nextInt(RandomIntBound),
        factor = nextDouble * nextInt(RandomIntBound),
        "SignalName" + i,
        "UnitName" + nextInt(UnitSize)
      )).toVector

  /**
   * Generates list of Measurements
   *
   * @param signals    each measurement is produced by a signal
   * @param randomness properties used to randomize a measurement
   * @return a list of Measurements
   */
  def generateMeasurements(signals: Vector[Signal], randomness: MeasurementRandomness): List[Measurement] =
    for {
      signal <- signals.toList
      _ <- 1 to nextInt(randomness.maxMeasurements)
    } yield Measurement(
      timeSec = nextInt(randomness.maxTimeSec),
      timeUSec = nextInt(MeasurementRandomness.UsecBound),
      signalId = signal.id,
      value = nextDouble * nextInt(RandomIntBound)
    )

  /**
   * Generates a complete binary file
   *
   * @param signalNumber          number of signals the file will contain
   * @param measurementRandomness properties used to randomize a measurement
   * @return randomized [[MyBinFile]]
   */
  def generateBinFile(signalNumber: Int, measurementRandomness: MeasurementRandomness): MyBinFile = {
    val header: Header = Header(signalNumber)
    val signals = generateSignals(header)
    val measurements = generateMeasurements(signals, measurementRandomness)
    MyBinFile(header, signals, measurements)
  }

}
