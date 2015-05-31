package workload.scenarios

import scala.language.reflectiveCalls

/**
 * Created by ebour.
 *
 */
class DynamicScenario {

  val scenarioClassName  = "workload.scenarios."+System.getProperty("scenario", "UndefScenario")
  println("Instantiating scenario: "+scenarioClassName)
  
  val scenarioClass = Class.forName(scenarioClassName)
  val scenario      = scenarioClass.newInstance.asInstanceOf[{ def getScenario: io.gatling.core.structure.ScenarioBuilder; def getDescription: String  }]
  println("Instantiating scenario: "+scenarioClassName+" [DONE]")
    
  def getScenario: io.gatling.core.structure.ScenarioBuilder = {
    scenario.getScenario
  }

  def getDescription: String = {
    scenario.getDescription
  }

  def getClassName: String = {
    scenarioClassName
  }
  
}
