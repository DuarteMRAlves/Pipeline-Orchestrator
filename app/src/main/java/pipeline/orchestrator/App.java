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
import pipeline.orchestrator.verification.errors.ErrorReport;

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
        ArchitectureParser.ParsingResult result;
        try {
            result = ArchitectureParser.parseYaml(
                    configuration.getConfigFile());
        } catch (IOException exception) {
            handleIOException(configuration, exception);
            return;
        }
        ErrorReport report = result.getReport();
        if (report.hasErrors()) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(report.summarize());
            return;
        }
        ValueGraph<StageInformation, LinkInformation> architecture = result.getArchitecture();
        new ExecutionOrchestrator(architecture).run();
    }

    private static void handleIOException(Configuration configuration, IOException exception) {
        if (exception instanceof FileNotFoundException) {
            LOGGER.error("File not found: '{}'", configuration.getConfigFile());
        }
        else {
            LOGGER.error("Unknown IOException when parsing pipeline architecture", exception);
        }
    }
}
