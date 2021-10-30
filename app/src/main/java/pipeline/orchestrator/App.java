package pipeline.orchestrator;

import com.google.common.graph.ValueGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.architecture.parsing.ArchitectureParser;
import pipeline.orchestrator.configuration.Configuration;
import pipeline.orchestrator.configuration.ConfigurationManager;
import pipeline.orchestrator.control.PipelineController;
import pipeline.orchestrator.verification.errors.ErrorReport;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(App.class);

    //private ExecutionOrchestrator orchestrator = null;
    private final PipelineController controller = new PipelineController();

    public static void main(String[] args) {
        new App().run(args);
    }

    public void run(String... args) {
        Optional<Configuration> optionalConfiguration =
                new ConfigurationManager().getConfiguration(args);
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
            handleIOExceptionAtParseConfiguration(configuration, exception);
            return;
        }
        ErrorReport report = result.getReport();
        if (report.hasErrors()) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(report.summarize());
            return;
        }
        ValueGraph<StageInformation, LinkInformation> architecture =
                result.getArchitecture();

        try {
            waitForStages(architecture.nodes());
        } catch (IOException exception) {
            LOGGER.error("Error while waiting for stages to start", exception);
            return;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return;
        }

        controller.updateGraph(architecture);
        controller.start();
        //orchestrator = new ExecutionOrchestrator(architecture);
        //orchestrator.run();
    }

    public void finish() {
        controller.finish();
//        if (orchestrator != null) {
//            orchestrator.finish();
//        }
    }

    private void handleIOExceptionAtParseConfiguration(
            Configuration configuration,
            IOException exception
    ) {
        if (exception instanceof FileNotFoundException) {
            LOGGER.error("File not found: '{}'", configuration.getConfigFile());
        }
        else {
            LOGGER.error("Unknown IOException when parsing pipeline architecture", exception);
        }
    }

    private void waitForStages(
            Iterable<StageInformation> stages
    ) throws IOException, InterruptedException {
        LOGGER.debug("Started waiting for stages");
        for (StageInformation stage : stages) {
            waitForStage(stage);
        }
        LOGGER.debug("Finished waiting for stages");
    }

    private void waitForStage(
            StageInformation stage
    ) throws IOException, InterruptedException {
        String target = stage.getServiceHost() + ":" + stage.getServicePort();
        LOGGER.info("Waiting for stage '{}' at '{}'.", stage.getName(), target);
        Process process = new ProcessBuilder()
                .command("./bin/wait-for-it.sh", "-t", "0", target)
                .start();

        Executors.newSingleThreadExecutor().submit(
                new StreamGobbler(process.getInputStream(), LOGGER::info)
        );
        Executors.newSingleThreadExecutor().submit(
                new StreamGobbler(process.getErrorStream(), LOGGER::info)
        );

        int status = process.waitFor();
        if (status != 0) {
            LOGGER.info("Status is {}", status);
        }
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }
}
