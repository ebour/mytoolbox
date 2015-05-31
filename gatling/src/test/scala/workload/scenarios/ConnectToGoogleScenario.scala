package workload.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._

/**
 * Created by ebour.
 *
 * mvn clean gatling:execute -Dgatling.simulationClass=workload.simulations.DynamicSimulation -Dscenario=ConnectToGoogleScenario -Drampup=AtOnceRampup -DuserCount=1 -Dtag="" -Durl="https://google.fr"
 */
class ConnectToGoogleScenario extends workload.scenarios.Scenario {

  override def getScenario: io.gatling.core.structure.ScenarioBuilder = {

    scenario("testing google")
      .exec(
        http("connect to Google")
        .get("/")
        .header("Connection", "keep-alive")
        .check(status.is(301)))
  }

  override def getDescription: String = {
    "testing google search page access"
  }

}
