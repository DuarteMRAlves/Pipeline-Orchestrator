package pipeline.orchestrator.configuration;

import java.util.Optional;

public class CommandLineArgumentsParser extends AbstractConfigurationParser {

    private final String configFile;

    public CommandLineArgumentsParser(String... args) {
        configFile = (args.length >= 1 ? args[0] : null);
    }

    @Override
    protected Optional<String> getConfigFile() {
        return Optional.ofNullable(configFile);
    }
}
