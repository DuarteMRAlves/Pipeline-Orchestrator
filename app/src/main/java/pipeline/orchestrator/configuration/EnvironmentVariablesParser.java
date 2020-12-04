package pipeline.orchestrator.configuration;

import java.util.Optional;

/**
 * Class to parse the environment variables and try to build
 * the app configuration
 */
public class EnvironmentVariablesParser extends AbstractConfigurationParser {

    private static final String STAGES_INFO_FILE = "STAGES_INFO_FILE";
    private static final String LINKS_INFO_FILE = "LINKS_INFO_FILE";

    @Override
    protected Optional<String> getStagesInfoFile() {
        return Optional.ofNullable(System.getenv(STAGES_INFO_FILE));
    }

    @Override
    protected Optional<String> getLinksInfoFile() {
        return Optional.ofNullable(System.getenv(LINKS_INFO_FILE));
    }
}
