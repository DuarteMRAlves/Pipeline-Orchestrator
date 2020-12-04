package pipeline.orchestrator.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Class responsible for the initialization parameters of the app
 */
public class ConfigurationManager {

    private static final Logger LOGGER = LogManager.getLogger(ConfigurationManager.class);

    /**
     *
     * @return the configuration for the application
     */
    public Optional<Configuration> getConfiguration() {
        Optional<Configuration> configuration;

        // Try and get configuration from environment
        configuration = new EnvironmentVariablesParser().buildAppConfiguration();
        if (configuration.isPresent()) {
            LOGGER.info("Getting configuration from environment: {}", configuration);
            return configuration;
        }

        // Try and get configuration from system properties
        configuration = new SystemPropertiesParser().buildAppConfiguration();
        if (configuration.isPresent()) {
            LOGGER.info("Getting configuration from system properties: {}", configuration);
            return configuration;
        }

        return Optional.empty();
    }
}
