package workload.rampups

import scala.language.reflectiveCalls

/**
 * Created by ebour.
 */
class DynamicRampup {
  
  val rampupClassName  = "workload.rampups."+System.getProperty("rampup", "UndefRampup")
  println("Instanciating rampup: "+rampupClassName)
  
  val rampupClass = Class.forName(rampupClassName)
  val rampup      = rampupClass.newInstance.asInstanceOf[{ def getRampup: io.gatling.core.controller.inject.InjectionStep; def getDescription: String }]
  println("Instanciating rampup: "+rampupClassName+" [DONE]")
  
  def getRampup: io.gatling.core.controller.inject.InjectionStep = {
    rampup.getRampup
  }

  def getDescription: String = {
    rampup.getDescription
  }

  def getClassName: String = {
    rampupClassName
  }

}
