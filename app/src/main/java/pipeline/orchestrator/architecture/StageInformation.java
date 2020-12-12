package pipeline.orchestrator.architecture;

import com.google.common.base.Preconditions;
import io.grpc.MethodDescriptor.MethodType;

import java.util.Optional;

public class StageInformation {

    private String name = null;
    private String serviceHost = null;
    private int servicePort = -1;

    private String methodName = null;
    private MethodType methodType = null;

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

    public Optional<String> getMethodName() {
        return Optional.ofNullable(methodName);
    }

    public MethodType getMethodType() {
        return methodType;
    }

    @Override
    public String toString() {
        return "StageInformation{" +
                "name='" + name + '\'' +
                ", serviceHost='" + serviceHost + '\'' +
                ", servicePort=" + servicePort +
                ", methodName='" + methodName + '\'' +
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

        public Builder setMethodName(String name) {
            current.methodName = name;
            return this;
        }

        public Builder setMethodType(MethodType type) {
            current.methodType = type;
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
            Preconditions.checkState(current.methodType != null);
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
            stageInformation.methodName = original.methodName;
            stageInformation.methodType = original.methodType;
            return stageInformation;
        }
    }
}
