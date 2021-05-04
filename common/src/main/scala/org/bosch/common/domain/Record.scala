package org.bosch.common.domain

final case class Record(filename: String, parameter: Parameter, timeArray: Array[Long], valueArray: Array[Float])
