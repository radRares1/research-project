package org.bosch.common.generators

import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}
import org.bosch.common.randomness.MeasurementRandomness

import scala.util.Random.{nextDouble, nextInt}

/**
 * helper object that generates signals, measurements and the whole file
 */
object Generator {

  val RandomIntBound = 10
  val UnitSize = 3
  val DefaultPath = "common/src/main/scala/org/bosch/common/out/file.txt"
  /**
   * function that generates a vector of signals
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
   * function that generates a list of Measurements
   * @param signals each measurement is produced by a signal
   * @param randomness object that holds the randomness properties of the measurement
   * @return a list of Measurements
   */
  def generateMeasurements(signals: Vector[Signal], randomness: MeasurementRandomness): List[Measurement] =
    for {
      signal <- signals.toList
      _ <- 1 to nextInt(randomness.maxMeasurements)
    } yield Measurement(
      timeSec = nextInt(randomness.maxTimeSec),
      timeUsec = nextInt(MeasurementRandomness.UsecBound),
      signalId = signal.id,
      value = nextDouble * nextInt(RandomIntBound)
    )


  /**
   * function that generates our MyBinFile
   * @param signalNumber number of signals the file will contain
   * @param measurementRandomness object that holds the randomness properties of the measurement
   * @return MyBinFile object with signalNumber signals and measurements created by those signals
   */
  def generateBinFile(signalNumber: Int, measurementRandomness: MeasurementRandomness): MyBinFile = {
    val header: Header = Header(signalNumber)
    val signals = generateSignals(header)
    val measurements = generateMeasurements(signals, measurementRandomness)
    MyBinFile(header, signals, measurements)
  }

}
