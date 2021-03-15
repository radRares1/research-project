package org.bosch.common.writer

import org.bosch.common.domain.MyBinFile
import org.bosch.common.generators.Generator.{DefaultPath, generateBinFile}
import org.bosch.common.randomness.MeasurementRandomness

import java.io.{BufferedOutputStream, FileOutputStream, IOException}

/**
 * object used for writing the binary file
 */
class BinFileWriter {

  /**
   * creates the MyBinFile object and writes it to a file
   * @param signalNumber number of signals in the file
   */
  def main(signalNumber:Int): Unit = {
    val MaxMeasurements: Int = 10000
    val randomness: MeasurementRandomness = MeasurementRandomness(MaxMeasurements)
    val binFile:MyBinFile = generateBinFile(signalNumber,randomness)
    writeToFile(binFile)
  }

  /**
   * method that encodes the MyBinFile instance and writes it to file
   * @param myBinFile MyBinFile instance
   * @param path path where the file will be stored
   */
  def writeToFile(myBinFile: MyBinFile,
                  path: String = DefaultPath): Unit = {
    try {
      val bytes = myBinFile.encode.require.bytes.toArray
      writeBytesToFile(java.nio.file.Paths.get(path).toString, bytes)
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }

  /**
   * method that writes an array of bytes to a file
   * @param fileOutput path where the file will be stored
   * @param bytes the array of bytes
   */
  def writeBytesToFile(fileOutput: String, bytes: Array[Byte]): Unit = {
    val fos = new BufferedOutputStream(new FileOutputStream(fileOutput))
    try {
      fos.write(bytes)
    }
    finally if (fos != null) fos.close()
  }

}
