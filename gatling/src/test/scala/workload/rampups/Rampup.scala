package workload.rampups

import io.gatling.core.Predef._


/**
 * Created by ebour.
 */
class Rampup {

  def getDescription: String = {
    "null"
  }

  def getRampup: io.gatling.core.scenario.InjectionStep = {
    atOnce(0 users)
  }

}
