package pipeline.orchestrator.architecture;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BuildStageInformationTest {

    private static final StageInformation.Builder BUILDER = StageInformation.newBuilder();
    private static final String HOST = "Host";
    private static final int PORT = 1;

    private static final String FIELD_1 = "Field 1";
    private static final String FIELD_2 = "Field 2";

    @Before
    public void setUp() {
        BUILDER.clear();
    }

    @Test
    public void buildServiceStageInformation() {
        StageInformation information = BUILDER.setServiceHost(HOST)
                .setServicePort(PORT)
                .build();

        assertEquals(HOST, information.getServiceHost());
        assertEquals(PORT, information.getServicePort());
    }

    @Test
    public void serviceStageMissingHost() {
        BUILDER.setServicePort(PORT);
        assertThrows(IllegalStateException.class, BUILDER::build);
    }

    @Test
    public void serviceStageMissingPort() {
        BUILDER.setServiceHost(HOST);
        assertThrows(IllegalStateException.class, BUILDER::build);
    }
}
