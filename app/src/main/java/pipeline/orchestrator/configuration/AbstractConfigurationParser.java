package pipeline.orchestrator.configuration;


import com.google.common.base.Preconditions;

import java.util.Optional;

/**
 * Abstract configuration parser
 * Implements the template method for parsing configuration
 */
public abstract class AbstractConfigurationParser {

    public final Optional<Configuration> buildAppConfiguration() {

        Optional<String> configFile = getConfigFile();

        // Should not init from environment
        if (configFile.isEmpty()) {
            return Optional.empty();
        }

        Configuration.Builder builder = Configuration.newBuilder()
                .setConfigFile(configFile.get());

        return Optional.of(builder.build());
    }

    protected abstract Optional<String> getConfigFile();
}
