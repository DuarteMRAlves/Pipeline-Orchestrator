package pipeline.orchestrator.configuration;

import java.util.Optional;

/**
 * Abstract configuration parser
 * Implements the template method for parsing configuration
 */
public abstract class AbstractConfigurationParser {

    public final Optional<Configuration> buildAppConfiguration() {

        Configuration.Builder builder = Configuration.newBuilder();

        return getConfigFile()
                .map(file -> builder.setConfigFile(file).build());
    }

    protected abstract Optional<String> getConfigFile();
}
