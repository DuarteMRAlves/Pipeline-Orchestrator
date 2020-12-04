package pipeline.orchestrator.architecture;

/**
 * Class to store the information connecting
 * two different stages
 * Saves the field that should be recovered from
 * the source stage and the field that should be filed
 * in the destination stage
 */
public class LinkInformation {

    private String sourceFieldName;
    private String targetFieldName;

    private LinkInformation() {}

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }


    @Override
    public String toString() {
        return "LinkInformation{" +
                "sourceFieldName='" + sourceFieldName + '\'' +
                ", targetFieldName='" + targetFieldName + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private LinkInformation current = new LinkInformation();

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
            return current;
        }
    }
}
