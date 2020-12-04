package pipeline.orchestrator.configuration;

public class Configuration {

    private String stagesInfoFile;
    private String stagesLinksFile;

    public String getStagesInfoFile() {
        return stagesInfoFile;
    }

    public String getStagesLinksFile() {
        return stagesLinksFile;
    }

    @Override
    public String toString() {
        return "AppConfiguration{" +
                "stagesInfoFile='" + stagesInfoFile + '\'' +
                ", stagesLinksFile='" + stagesLinksFile + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Configuration current = new Configuration();

        private Builder() {}

        public Builder setStagesInfoFile(String stagesInfoFile) {
            current.stagesInfoFile = stagesInfoFile;
            return this;
        }

        public Builder setStagesLinksFile(String stagesLinksFile) {
            current.stagesLinksFile = stagesLinksFile;
            return this;
        }

        public Builder clear() {
            current = new Configuration();
            return this;
        }

        public Configuration build() {
            return copy(current);
        }

        private Configuration copy(Configuration original) {
            Configuration copy = new Configuration();
            copy.stagesInfoFile = original.stagesInfoFile;
            copy.stagesLinksFile = original.stagesLinksFile;
            return copy;
        }
    }
}
