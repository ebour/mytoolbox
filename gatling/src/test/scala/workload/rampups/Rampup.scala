package workload.rampups

import io.gatling.core.Predef._


/**
 * Created by ebour.
 */
class Rampup {

  def getDescription: String = {
    "null"
  }

  def getRampup: io.gatling.core.controller.inject.InjectionStep = {
    atOnceUsers(0)
  }

}
