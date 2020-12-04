package pipeline.orchestrator.execution.inputs;

import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.Link;

public class SingleInputStream implements StageInputStream {

    private final Link inputLink;

    SingleInputStream(Link inputLink) {
        this.inputLink = inputLink;
    }

    @Override
    public DynamicMessage get() {
        try {
            return inputLink.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
