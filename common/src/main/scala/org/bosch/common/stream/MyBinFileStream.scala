package org.bosch.common.stream


import org.bosch.common.domain.{Header, Measurement}
import org.bosch.common.generators.{Generator, MeasurementRandomness}
import scodec.stream._
import fs2.{Fallible, Stream}

//ignore this file it was just an experiment area
object MyBinFileStream extends App{

  override def main(args: Array[String]): Unit = {
    val randomness: MeasurementRandomness = MeasurementRandomness(Int.MaxValue)
    val encoder = StreamEncoder.many(Measurement.codec)
    val decoder = StreamDecoder.many(Measurement.codec)

    val s = Generator.generateStreamMeasurements(Generator.generateSignals(Header(3)),randomness)
    val stream = Stream.unfold(s) {
      case hd #:: tl => Some((hd, tl))
      case _ => None
    }
    val b = encoder.encode(stream.covary[Fallible])
    println(b.take(1).toList)
    val decoded = decoder.decode(b)
    print(decoded.take(1).toList)

  }

}
