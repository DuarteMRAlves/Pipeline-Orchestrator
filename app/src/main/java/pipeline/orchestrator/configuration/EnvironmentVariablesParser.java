package pipeline.orchestrator.configuration;

import java.util.Optional;

/**
 * Class to parse the environment variables and try to build
 * the app configuration
 */
public class EnvironmentVariablesParser extends AbstractConfigurationParser {

    private static final String CONFIG_FILE = "CONFIG_FILE";

    @Override
    protected Optional<String> getConfigFile() {
        return Optional.ofNullable(System.getenv(CONFIG_FILE));
    }
}
