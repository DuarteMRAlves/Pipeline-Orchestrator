package pipeline.orchestrator.execution.stages;

import com.google.common.eventbus.EventBus;
import io.grpc.Channel;
import pipeline.orchestrator.grpc.methods.FullMethodDescription;

/**
 * Abstract builder class for stages
 * Defines setters and getters for common attributes for
 * all stages
 */
public abstract class ExecutionStageBuilder<T extends ExecutionStage> {

    // Name of the stage
    private String name;

    // Channel for the stage to use
    private Channel channel;

    // Full method description for the method
    // that this stage will execute
    private FullMethodDescription description;

    // Event bus for the stage to use when
    // publishing events
    private EventBus eventBus;

    public ExecutionStageBuilder<T> setName(String name) {
        this.name = name;
        return this;
    }

    protected String getName() {
        return name;
    }

    public ExecutionStageBuilder<T> setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    protected Channel getChannel() {
        return channel;
    }

    public ExecutionStageBuilder<T> setFullMethodDescription(
            FullMethodDescription description) {

        this.description = description;
        return this;
    }

    protected FullMethodDescription getDescription() {
        return description;
    }

    public ExecutionStageBuilder<T> setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        return this;
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Builds a new stage instance
     * @return the new stage
     */
    public abstract T build();
}
