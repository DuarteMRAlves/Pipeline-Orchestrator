package pipeline.orchestrator.architecture;

import com.google.common.base.Preconditions;

import java.util.Optional;

/**
 * Class to collect information about a stage
 */
public class StageInformation {

    private String name = null;
    private String serviceHost = null;
    private int servicePort = -1;

    private String serviceName = null;
    private String methodName = null;

    /**
     * Specifies if the stage method should only be executed one time.
     * True if the stage is only executed once and false otherwise.
     */
    private boolean oneShot = false;

    private StageInformation() {}

    public String getName() {
        return name;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public int getServicePort() {
        return servicePort;
    }

    public Optional<String> getServiceName() {
        return Optional.ofNullable(serviceName);
    }

    public Optional<String> getMethodName() {
        return Optional.ofNullable(methodName);
    }

    public boolean isOneShot() {
        return oneShot;
    }

    @Override
    public String toString() {
        return "StageInformation{" +
                "name='" + name + '\'' +
                ", serviceHost='" + serviceHost + '\'' +
                ", servicePort=" + servicePort +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", oneShot=" + oneShot +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private StageInformation current = new StageInformation();

        public Builder setName(String name) {
            current.name = name;
            return this;
        }

        public Builder setServiceHost(String serviceHost) {
            current.serviceHost = serviceHost;
            return this;
        }

        public Builder setServicePort(int servicePort) {
            current.servicePort = servicePort;
            return this;
        }

        public Builder setServiceName(String name) {
            current.serviceName = name;
            return this;
        }

        public Builder setMethodName(String name) {
            current.methodName = name;
            return this;
        }

        public Builder setOneShot(boolean oneShot) {
            current.oneShot = oneShot;
            return this;
        }

        public Builder from(StageInformation stageInformation) {
            current = copy(stageInformation);
            return this;
        }

        public StageInformation build() {
            Preconditions.checkState(current.name != null);
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
            stageInformation.name = original.name;
            stageInformation.serviceHost = original.serviceHost;
            stageInformation.servicePort = original.servicePort;
            stageInformation.serviceName = original.serviceName;
            stageInformation.methodName = original.methodName;
            stageInformation.oneShot = original.oneShot;
            return stageInformation;
        }
    }
}
