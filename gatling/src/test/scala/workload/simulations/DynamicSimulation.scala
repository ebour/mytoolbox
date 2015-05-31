package workload.simulations

import io.gatling.http.Predef._

import java.sql.Timestamp;
import java.util.Date;

import workload.rampups.DynamicRampup
import workload.scenarios.DynamicScenario

/**
 * Created by ebour.
 */
class DynamicSimulation extends io.gatling.core.scenario.Simulation {

    // Init objects for simulation
    // System.setProperty("javax.net.ssl.truststore", "");
    // System.setProperty("javax.net.ssl.trustStorePassword", "");

    val httpProtocol = http.baseURL(getUrl).disableFollowRedirect.disableCaching.disableClientSharing.warmUp(getUrl)
    val rampup = new DynamicRampup()
    val scenario = new DynamicScenario()

    val timestamp = new Timestamp(new Date().getTime())

    // Showing details on the simulation
    println("simulation {\n\tscenario: {\n\t\tclassName: "+scenario.getClassName+"\n\t\tdescription: "+scenario.getDescription+"\n\t}\n\trampup: {\n\t\tclassName: "+rampup.getClassName+"\n\t\tdescription: "+rampup.getDescription+"\n\t}\n\ttimestamp: "+timestamp+"\n}")


    // Launch the simulation
    setUp(scenario.getScenario.inject(rampup.getRampup)).protocols(httpProtocol)
        //.assertions(global.failedRequests.percent.is(0))
    
    def getUrl: String = {
      System.getProperty("url", "UndefUrl")
    }
}
