package workload.rampups

import io.gatling.core.Predef._
import scala.concurrent.duration._

/**
 * Created by ebour.
 */
class HeavisideRampup extends workload.rampups.Rampup {

  val userCount = System.getProperty("userCount", "1").toInt
  val sDuration = System.getProperty("sDuration", "60").toInt

  override def getRampup: io.gatling.core.controller.inject.InjectionStep = {
    heavisideUsers(userCount) over (sDuration seconds)
  }

  override def getDescription: String = {
    userCount+" users are injected at once and the load stays still for "+sDuration+" seconds"
  }

}
