package pipeline.orchestrator.execution.inputs;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

public class SourceInputStream implements StageInputStream {

    private final Descriptors.Descriptor descriptor;

    SourceInputStream(Descriptors.Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public DynamicMessage get() {
        return DynamicMessage.getDefaultInstance(descriptor);
    }
}
