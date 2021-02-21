package domain

final case class Record(filename: String, parameter: Parameter, timeVector: Array[Long], valueVector: Array[Float])
