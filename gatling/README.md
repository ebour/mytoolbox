# mytoolbox-gatling

* usage

`mvn clean gatling:execute 
                -Dgatling.simulationClass=workload.simulations.DynamicSimulation 
                -Dscenario=ConnectToGoogleScenario 
                -Drampup=AtOnceRampup 
                -DuserCount=1 
                -Dtag="perf testing with gatling 2.0.0-M3" 
                -Durl=http://google.fr"`
