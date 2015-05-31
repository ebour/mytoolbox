package workload.scenarios

import io.gatling.core.Predef._

/**
 * Created by ebour.
 */
class Scenario extends io.gatling.core.scenario.Simulation {

    def getScenario: io.gatling.core.structure.ScenarioBuilder = {
      scenario("null")
    }

    def getDescription: String = {
      "null"
    }
    
}
