package pipeline.orchestrator.grpc.methods;

import com.google.protobuf.Descriptors.MethodDescriptor;

public class FullMethodDescription {

    private MethodDescriptor methodDescriptor;
    private String methodFullName;

    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    public String getMethodFullName() {
        return methodFullName;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private final FullMethodDescription current = new FullMethodDescription();

        public Builder setMethodDescriptor(MethodDescriptor methodDescriptor) {
            current.methodDescriptor = methodDescriptor;
            return this;
        }

        public Builder setMethodFullName(String methodFullName) {
            current.methodFullName = methodFullName;
            return this;
        }

        public FullMethodDescription build() {
            return copy(current);
        }

        private FullMethodDescription copy(FullMethodDescription source) {
            FullMethodDescription target = new FullMethodDescription();
            target.methodDescriptor = source.methodDescriptor;
            target.methodFullName = source.methodFullName;
            return target;
        }
    }
}
