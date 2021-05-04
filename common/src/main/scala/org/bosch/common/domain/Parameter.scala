package org.bosch.common.domain

final case class Parameter(name:String, unit: String)

object Parameter{
  def apply(signal:Signal):Parameter =  {
      new Parameter(signal.name,signal.unit)
  }
}
