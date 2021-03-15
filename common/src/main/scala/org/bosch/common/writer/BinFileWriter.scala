package org.bosch.common.writer

import org.bosch.common.domain.MyBinFile
import org.bosch.common.generators.Generator.{DefaultPath, generateBinFile}
import org.bosch.common.randomness.MeasurementRandomness

import java.io.{BufferedOutputStream, FileOutputStream, IOException}
import java.nio.file.Paths
import scala.io.StdIn

/**
 * object used for writing the binary file
 */
object BinFileWriter {

  /**
   * creates the MyBinFile object and writes it to a file
   */
  def main(args: Array[String]): Unit = {
    val signalNumber: Int = StdIn.readLine("please input the number of signals: ").toInt
    val maxMeasurements: Int = StdIn.readLine("please input the maximum number of measurements: ").toInt
    val randomness: MeasurementRandomness = MeasurementRandomness(maxMeasurements)
    val binFile: MyBinFile = generateBinFile(signalNumber, randomness)
    writeToFile(binFile)
  }

  /**
   * method that encodes the MyBinFile instance and writes it to file
   *
   * @param myBinFile MyBinFile instance
   * @param path      path where the file will be stored
   */
  def writeToFile(myBinFile: MyBinFile,
                  path: String = DefaultPath): Unit = {

    val bytes = myBinFile.encode.require.bytes.toArray
    writeBytesToFile(Paths.get(path).toString, bytes)
  }

  /**
   * method that writes an array of bytes to a file
   *
   * @param fileOutput path where the file will be stored
   * @param bytes      the array of bytes
   */
  def writeBytesToFile(fileOutput: String, bytes: Array[Byte]): Unit = {
    val fos = new BufferedOutputStream(new FileOutputStream(fileOutput))
    try {
      fos.write(bytes)
    }
    finally if (fos != null) fos.close()
  }

}
