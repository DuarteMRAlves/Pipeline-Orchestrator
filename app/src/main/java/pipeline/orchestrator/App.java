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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);

    public static void main(String[] args) {

        Optional<Configuration> optionalConfiguration = new ConfigurationManager().getConfiguration();
        if (optionalConfiguration.isEmpty()) {
            LOGGER.error("Missing configuration file");
            return;
        }
        Configuration configuration = optionalConfiguration.get();
        LOGGER.info(configuration);
        ValueGraph<StageInformation, LinkInformation> architecture;
        try {
            architecture = ArchitectureParser.parseYaml(
                    configuration.getConfigFile());
        } catch (IOException exception) {
            handleIOException(configuration, exception);
            return;
        }
        new ExecutionOrchestrator(architecture).run();
    }

    private static void handleIOException(Configuration configuration, IOException exception) {
        if (exception instanceof FileNotFoundException) {
            LOGGER.error("File not found: '{}'", configuration.getConfigFile());
        }
        else {
            LOGGER.error("IOException when parsing pipeline architecture", exception);
        }
    }
}
