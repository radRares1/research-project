package org.bosch.common.generators

import cats.effect.IO
import fs2.{Pure, Stream}
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

  /**
   * Generates a fs2 Stream of Measurements
   * @param signals each measurement is produced by a signal
   * @param randomness properties used to randomize a measurement
   * @return fs Stream of Measurements
   */
  def generateStreamMeasurements(signals: Signals, randomness: MeasurementRandomness, chunkSize:Int): Stream[IO,List[Measurement]] = {
    val USecBound = 999999
    val ourStream = for {
      _ <- (1 to nextInt(randomness.maxMeasurements)).iterator
      signal <- signals
    } yield Measurement(
      timeSec = nextInt(randomness.maxTimeSec),
      timeUSec = nextInt(USecBound),
      signalId = signal.id,
      value = nextDouble
    )
    Stream.fromIterator[IO](ourStream.grouped(chunkSize).map(_.toList))
  }
// alternative solution
//  def anotherStream(signals: Vector[Signal], randomness: MeasurementRandomness): Stream[Pure,Measurement] =
//    Stream.emits(signals.map(s => getMeasurementsForSignal(s,randomness))).flatten
//
//
//  def getMeasurementsForSignal(signal:Signal,randomness: MeasurementRandomness): Stream[Pure,Measurement] = {
//    val USecBound = 999999
//    Stream.emits(1 to nextInt(randomness.maxMeasurements)).map( id => Measurement(
//      timeSec = nextInt(randomness.maxTimeSec),
//      timeUSec = nextInt(USecBound),
//      signalId = signal.id,
//      value = nextDouble
//    ))
//  }

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
