package org.bosch.common.generators

import org.bosch.common.domain.{Header, Measurement, MyBinFile, Signal}
import scodec.codecs.byte

import scala.util.Random.{nextDouble, nextInt}
import java.io._

object Generator {

  val RandomIntBound = 10
  val UnitSize = 3
  val MeasurementBound = 100000
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

  def generateMeasurements(signals: Vector[Signal]): List[Measurement] =
    (for {
      signal <- signals
      _ <- 1 to nextInt(MeasurementBound)
      } yield Measurement(
        timeSec = nextInt(SecBound),
        timeUsec = nextInt(UsecBound),
        signalId = signal.id,
        value = nextDouble * nextInt(RandomIntBound))
      ).toList

  def writeToFile(header:Header, signals:Vector[Signal], measurements:List[Measurement], path:String = "common/src/main/scala/org/bosch/common/out"):Unit = {

    val bytes = MyBinFile(header,signals,measurements).encode.require.bytes.toArray

    try {
      writeBytesToFile( path + "/file1.txt",bytes)
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }

  @throws[IOException]
  private def writeBytesToFile(fileOutput: String, bytes: Array[Byte]): Unit = {
    try {
      val fos = new BufferedOutputStream(new FileOutputStream(fileOutput),16384)
      try fos.write(bytes)
      finally if (fos != null) fos.close()
    }
  }


}
