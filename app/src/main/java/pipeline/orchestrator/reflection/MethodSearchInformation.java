package pipeline.orchestrator.reflection;

import java.util.Optional;

public final class MethodSearchInformation {

    private final String serviceName;
    private final String methodName;

    private MethodSearchInformation(Builder builder) {
        this.serviceName = builder.serviceName;
        this.methodName = builder.methodName;
    }

    public Optional<String> getServiceName() {
        return Optional.ofNullable(serviceName);
    }

    public Optional<String> getMethodName() {
        return Optional.ofNullable(methodName);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String serviceName;
        private String methodName;

        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setMethodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public MethodSearchInformation build() {
            return new MethodSearchInformation(this);
        }
    }
}
