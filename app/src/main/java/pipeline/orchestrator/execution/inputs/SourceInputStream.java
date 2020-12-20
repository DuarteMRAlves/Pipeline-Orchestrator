package pipeline.orchestrator.execution.inputs;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.ComputationState;

public class SourceInputStream implements StageInputStream {

    private final Descriptors.Descriptor descriptor;
    private int currentId = 1;

    SourceInputStream(Descriptors.Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    /**
     * @return new computation state with an increasing id
     *         and the default message for the descriptor
     */
    @Override
    public ComputationState get() {
        synchronized (this) {
            return ComputationState.from(
                    currentId++,
                    DynamicMessage.getDefaultInstance(descriptor));
        }
    }
}
