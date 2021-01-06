package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Preconditions;
import com.google.common.graph.*;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;
import pipeline.orchestrator.verification.Verifications;
import pipeline.orchestrator.verification.errors.ErrorReport;

import java.io.*;
import java.util.Optional;

/**
 * Class to parse the architecture from different file formats
 */
public class ArchitectureParser {

    private ArchitectureParser() {}

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Parses the system architecture from a yaml configuration file
     * @param configFile path to the configuration file
     * @return parsing result for the architecture. The result has an error
     *         report with the errors that occurred when building the graph.
     *         If any error occurred, the graph is not build
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if the configuration file is null
     */
    public static ParsingResult parseYaml(
            String configFile)
            throws IOException {

        Preconditions.checkNotNull(configFile);

        ArchitectureInformationDto dto = YAML_MAPPER.readValue(
                new File(configFile),
                ArchitectureInformationDto.class);

        // Verify if necessary conditions for building
        // graph are satisfied
        ErrorReport report = Verifications.exhaustiveVerification(dto);

        return new ParsingResult(
                !report.hasErrors() ? ArchitectureGraphBuilder.build(dto) : null,
                report
        );
    }

    public static class ParsingResult {

        private final ImmutableValueGraph<StageInformation, LinkInformation> architecture;

        private final ErrorReport report;

        private ParsingResult(
                ImmutableValueGraph<StageInformation, LinkInformation> architecture,
                ErrorReport report) {
            this.architecture = architecture;
            this.report = report;
        }

        public ImmutableValueGraph<StageInformation, LinkInformation> getArchitecture() {
            return architecture;
        }

        public ErrorReport getReport() {
            return report;
        }
    }
}
