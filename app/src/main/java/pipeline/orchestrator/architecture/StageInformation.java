package pipeline.orchestrator.architecture;

import com.google.common.base.Preconditions;

public class StageInformation {

    private String serviceHost = null;
    private int servicePort = -1;
    private String methodName = null;

    private StageInformation() {}

    public String getServiceHost() {
        return serviceHost;
    }

    public int getServicePort() {
        return servicePort;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "StageInformation{"
                + "host: " + serviceHost
                + ", port: " + servicePort
                + (methodName != null ? ", methodName: " + methodName : "")
                + "}";
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private StageInformation current = new StageInformation();

        public Builder setServiceHost(String serviceHost) {
            current.serviceHost = serviceHost;
            return this;
        }

        public Builder setServicePort(int servicePort) {
            current.servicePort = servicePort;
            return this;
        }

        public Builder setMethodName(String name) {
            current.methodName = name;
            return this;
        }

        public Builder from(StageInformation stageInformation) {
            current = copy(stageInformation);
            return this;
        }

        public StageInformation build() {
            Preconditions.checkState(current.serviceHost != null);
            Preconditions.checkState(current.servicePort != -1);
            return copy(current);
        }

        public Builder clear() {
            current = new StageInformation();
            return this;
        }

        private StageInformation copy(StageInformation original) {
            StageInformation stageInformation = new StageInformation();
            stageInformation.serviceHost = original.serviceHost;
            stageInformation.servicePort = original.servicePort;
            stageInformation.methodName = original.methodName;
            return stageInformation;
        }
    }
}
