package pipeline.orchestrator.configuration;

import java.util.Optional;

public class SystemPropertiesParser extends AbstractConfigurationParser {

    private static final String CONFIG_FILE = "configFile";

    @Override
    protected Optional<String> getConfigFile() {
        return Optional.ofNullable(System.getProperty(CONFIG_FILE));
    }
}
