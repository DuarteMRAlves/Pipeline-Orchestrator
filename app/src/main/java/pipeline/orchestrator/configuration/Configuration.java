package pipeline.orchestrator.configuration;

public class Configuration {

    private String configFile;

    public String getConfigFile() {
        return configFile;
    }

    @Override
    public String toString() {
        return "AppConfiguration{" +
                "configFile='" + configFile + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Configuration current = new Configuration();

        private Builder() {}

        public Builder setConfigFile(String stagesInfoFile) {
            current.configFile = stagesInfoFile;
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
            copy.configFile = original.configFile;
            return copy;
        }
    }
}
