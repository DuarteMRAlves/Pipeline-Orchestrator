package pipeline.orchestrator.configuration;


import com.google.common.base.Preconditions;

import java.util.Optional;

/**
 * Abstract configuration parser
 * Implements the template method for parsing configuration
 */
public abstract class AbstractConfigurationParser {

    public final Optional<Configuration> buildAppConfiguration() {

        Optional<String> stagesInfoFile = getStagesInfoFile();
        Optional<String> linksInfoFile = getLinksInfoFile();

        // Should not init from environment
        if (stagesInfoFile.isEmpty() && linksInfoFile.isEmpty()) {
            return Optional.empty();
        }
        // Check if all params are set
        Preconditions.checkState(stagesInfoFile.isPresent() && linksInfoFile.isPresent());

        Configuration.Builder builder = Configuration.newBuilder()
                .setStagesInfoFile(stagesInfoFile.get())
                .setStagesLinksFile(linksInfoFile.get());

        return Optional.of(builder.build());
    }

    protected abstract Optional<String> getStagesInfoFile();

    protected abstract Optional<String> getLinksInfoFile();
}
