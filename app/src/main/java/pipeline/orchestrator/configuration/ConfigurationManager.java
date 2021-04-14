package pipeline.orchestrator.configuration;

import java.util.Optional;

/**
 * Class responsible for the initialization parameters of the app
 */
public class ConfigurationManager {

    /**
     *
     * @return the configuration for the application.
     *         Returns empty if the configuration could not be loaded.
     */
    public Optional<Configuration> getConfiguration(String... args) {
        // First get configuration from environment
        return getConfigurationFromEnvironment()
                // Try get from system properties
                .or(this::getConfigurationFromSystemProperties)
                // Try get from command line arguments
                .or(() -> getConfigurationFromCommandLineArguments(args));
    }

    private Optional<Configuration> getConfigurationFromEnvironment() {
        return new EnvironmentVariablesParser().buildAppConfiguration();
    }

    private Optional<Configuration> getConfigurationFromSystemProperties() {
        return new SystemPropertiesParser().buildAppConfiguration();
    }

    private Optional<Configuration> getConfigurationFromCommandLineArguments(
            String... args
    ) {
        return new CommandLineArgumentsParser(args).buildAppConfiguration();
    }
}
