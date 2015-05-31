package workload.rampups

import io.gatling.core.Predef._

/**
 * Created by ebour.
 */
class AtOnceRampup extends workload.rampups.Rampup {

  val userCount = System.getProperty("userCount", "1").toInt

  override def getRampup: io.gatling.core.scenario.InjectionStep = {
    atOnce(userCount users)
  }

  override def getDescription: String = {
    userCount+" users are injected at once"
  }

}