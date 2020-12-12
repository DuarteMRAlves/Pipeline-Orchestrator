package pipeline.orchestrator.architecture;

import com.google.common.base.Preconditions;

import java.util.Optional;

/**
 * Class to store the information connecting
 * two different stages
 * Saves the field that should be recovered from
 * the source stage and the field that should be filed
 * in the destination stage
 */
public class LinkInformation {

    // Name of the source stage
    private String sourceStageName;

    // Name of the target stage
    private String targetStageName;

    // Field to get from the message when the source is sending the message
    // If not set then the whole message will be sent
    private String sourceFieldName;

    // Field to set in the message being created in the target
    // If not set than the created message should be everything received
    private String targetFieldName;

    private LinkInformation() {}

    public String getSourceStageName() {
        return sourceStageName;
    }

    public String getTargetStageName() {
        return targetStageName;
    }

    public Optional<String> getSourceFieldName() {
        return Optional.ofNullable(sourceFieldName);
    }

    public Optional<String> getTargetFieldName() {
        return Optional.ofNullable(targetFieldName);
    }

    @Override
    public String toString() {
        return "LinkInformation{" +
                "sourceStageName='" + sourceStageName + '\'' +
                ", targetStageName='" + targetStageName + '\'' +
                ", sourceFieldName='" + sourceFieldName + '\'' +
                ", targetFieldName='" + targetFieldName + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private LinkInformation current = new LinkInformation();

        public Builder setSourceStageName(String sourceStageName) {
            current.sourceStageName = sourceStageName;
            return this;
        }

        public Builder setTargetStageName(String targetStageName) {
            current.targetStageName = targetStageName;
            return this;
        }

        public Builder setSourceFieldName(String sourceFieldName) {
            current.sourceFieldName = sourceFieldName;
            return this;
        }

        public Builder setTargetFieldName(String targetFieldName) {
            current.targetFieldName = targetFieldName;
            return this;
        }

        public Builder clear() {
            current = new LinkInformation();
            return this;
        }

        public LinkInformation build() {
            Preconditions.checkNotNull(current.sourceStageName);
            Preconditions.checkNotNull(current.targetStageName);
            return copy(current);
        }

        private LinkInformation copy(LinkInformation original) {
            LinkInformation copy = new LinkInformation();
            copy.sourceStageName = original.sourceStageName;
            copy.targetStageName = original.targetStageName;
            copy.sourceFieldName = original.sourceFieldName;
            copy.targetFieldName = original.targetFieldName;
            return copy;
        }
    }
}
