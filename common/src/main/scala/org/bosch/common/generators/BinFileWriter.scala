package org.bosch.common.generators

import java.io.{BufferedOutputStream, FileOutputStream}
import java.nio.file.Paths

import org.bosch.common.domain.MyBinFile
import org.bosch.common.generators.Generator.generateBinFile

import scala.io.StdIn

/** Entry point of the application for generating and writing a binary file */
object BinFileWriter {

  val DefaultPath = "common/src/main/scala/org/bosch/common/out/file.txt"

  /** Creates a MyBinFile and writes it to a file */
  def main(args: Array[String]): Unit = {
    val signalNumber: Int = StdIn.readLine("Please input the number of signals: ").toInt
    val maxMeasurements: Int = StdIn.readLine("Please input the maximum number of measurements: ").toInt
    val path: String = StdIn.readLine("Please input where to save the file")
    val randomness: MeasurementRandomness = MeasurementRandomness(maxMeasurements)
    val binFile: MyBinFile = generateBinFile(signalNumber, randomness)
    writeToFile(binFile, path)
  }

  /**
   * Encodes a [[MyBinFile]] and writes it to file
   *
   * @param myBinFile [[MyBinFile]] instance
   * @param path      path where the file will be stored
   */
  def writeToFile(myBinFile: MyBinFile, path: String = DefaultPath): Unit = {
    val bytes: Array[Byte] = myBinFile.encode.require.bytes.toArray
    writeBytesToFile(Paths.get(path).toString, bytes)
  }

  /**
   * Writes an array of bytes to a file
   *
   * @param fileOutput path where the file will be stored
   * @param bytes      array of bytes
   */
  def writeBytesToFile(fileOutput: String, bytes: Array[Byte]): Unit = {
    val fos = new BufferedOutputStream(new FileOutputStream(fileOutput))
    try {
      fos.write(bytes)
    } finally fos.close()
  }

}
