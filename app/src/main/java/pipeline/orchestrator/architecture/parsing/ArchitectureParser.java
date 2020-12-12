package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Preconditions;
import com.google.common.graph.*;
import pipeline.orchestrator.architecture.LinkInformation;
import pipeline.orchestrator.architecture.StageInformation;

import java.io.*;

/**
 * Class to parse the architecture from different file formats
 */
public class ArchitectureParser {

    private ArchitectureParser() {}

    private static final VerifyObjectMapper YAML_MAPPER = new VerifyObjectMapper(new YAMLFactory());

    /**
     * Parses the system architecture from a yaml configuration file
     * @param configFile path to the configuration file
     * @return the graph with the architecture
     * @throws IOException if an I/O error occurs
     * @throws NullPointerException if the configuration file is null
     */
    public static ImmutableValueGraph<StageInformation, LinkInformation> parseYaml(
            String configFile)
            throws IOException {

        Preconditions.checkNotNull(configFile);

        ArchitectureInformationDto dto = YAML_MAPPER.readValue(
                new File(configFile),
                ArchitectureInformationDto.class);
        return ArchitectureGraphBuilder.build(dto);
    }
}
