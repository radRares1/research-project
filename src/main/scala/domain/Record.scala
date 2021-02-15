package org.bosch
package domain

case class Record(filename: String, parameter: Parameter, timeVector: Array[Long], valueVector: Array[Float])
