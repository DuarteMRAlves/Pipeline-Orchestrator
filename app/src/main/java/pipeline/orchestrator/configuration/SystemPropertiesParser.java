package pipeline.orchestrator.configuration;

import java.util.Optional;

public class SystemPropertiesParser extends AbstractConfigurationParser {

    private static final String STAGES_INFO_FILE = "stagesInfoFile";
    private static final String LINKS_INFO_FILE = "linksInfoFile";

    @Override
    protected Optional<String> getStagesInfoFile() {
        return Optional.ofNullable(System.getProperty(STAGES_INFO_FILE));
    }

    @Override
    protected Optional<String> getLinksInfoFile() {
        return Optional.ofNullable(System.getProperty(LINKS_INFO_FILE));
    }
}
