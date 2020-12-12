package pipeline.orchestrator;

import com.google.common.graph.ValueGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.architecture.parsing.ArchitectureParser;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.configuration.Configuration;
import pipeline.orchestrator.configuration.ConfigurationManager;
import pipeline.orchestrator.execution.ExecutionOrchestrator;

import java.io.IOException;
import java.util.Optional;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);

    public static void main(String[] args) {

        Optional<Configuration> optionalConfiguration = new ConfigurationManager().getConfiguration();
        if (optionalConfiguration.isEmpty()) {
            LOGGER.error("No configuration found");
            return;
        }
        Configuration configuration = optionalConfiguration.get();
        ValueGraph<StageInformation, LinkInformation> architecture = null;
        try {
            architecture = ArchitectureParser.parseYaml(
                    configuration.getConfigFile());
        } catch (IOException exception) {
            LOGGER.error("Unable to parse pipeline architecture", exception);
            System.exit(1);
        }
        new ExecutionOrchestrator(architecture).run();
    }
}
