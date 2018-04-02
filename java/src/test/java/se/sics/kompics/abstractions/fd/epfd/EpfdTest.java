package se.sics.kompics.abstractions.fd.epfd;

import org.junit.Test;
import se.sics.kompics.abstractions.TestUtils;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.result.SimulationResultMap;
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.simulator.run.LauncherComp;

public class EpfdTest {

    @Test
    public void simulationTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario epfdScenario = EpfdScenario.oneFailure(TestUtils.EPFD_NODES);
        epfdScenario.simulate(LauncherComp.class);
        SimulationResultMap res = SimulationResultSingleton.getInstance();
    }
}
