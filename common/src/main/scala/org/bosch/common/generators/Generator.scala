package org.bosch.common.generators

import org.bosch.common.domain.{Header, Measurement, Signal}

import scala.util.Random.{nextDouble, nextInt}

object Generator {

  val RandomIntBound = 10
  val UnitSize = 3
  val MeasurementBound = 100
  val SecBound = 2000000000
  val UsecBound = 999999

  def generateHeader(signalNumber: Int): Header = Header(signalNumber)

  def generateSignals(header: Header): Vector[Signal] =
    (for (i <- 1 to header.signalNumber) yield
      Signal(
        id = i,
        offset = nextDouble * nextInt(RandomIntBound),
        factor = nextDouble * nextInt(RandomIntBound),
        "SignalName" + i,
        "UnitName" + nextInt(UnitSize)
      )
      ).toVector

  def generateMeasurements: List[Measurement] = {

    //todo this should be a random number of signals or something similar
    val header: Header = generateHeader(3)
    val signals: Vector[Signal] = generateSignals(header)
    val noOfMeasurementsForSignal = for (i <- 1 to signals.length) yield nextInt(MeasurementBound)

    signals.flatMap(s => {
      for (i <- noOfMeasurementsForSignal) yield {
        for (j <- 1 to i) yield {
          Measurement(
            timeSec = nextInt(SecBound),
            timeUsec = nextInt(UsecBound),
            signalId = s.id,
            value = nextDouble * nextInt(RandomIntBound))
        }
      }
    }).toList.flatten

  }


}
