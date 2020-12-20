package pipeline.orchestrator.execution;

import com.google.protobuf.DynamicMessage;

/**
 * Object with the computation state
 * Has an id to uniquely identify the computation
 * This id should be kept throughout the merge and split operations
 * so that the state can be uniquely identified
 * Also stores the current computation state
 */
public class ComputationState {

    private final int id;

    private final DynamicMessage message;

    private ComputationState(int id, DynamicMessage message) {
        this.id = id;
        this.message = message;
    }

    /**
     * Creates a new ComputationState with the given id and messages
     * @param id id for the computation
     * @param dynamicMessage message with the current computation state
     * @return the new computation state
     */
    public static ComputationState from(int id, DynamicMessage dynamicMessage) {
        return new ComputationState(id, dynamicMessage);
    }

    /**
     * Creates a new computation state from the old
     * The new state is a followup of the previous state where the message is updated
     * @param previous previous state that defines the computation id
     * @param dynamicMessage new message for the computation state
     * @return a new computation state with the previous id and new message
     */
    public static ComputationState from(
            ComputationState previous,
            DynamicMessage dynamicMessage) {

        return new ComputationState(previous.id, dynamicMessage);
    }

    public int getId() {
        return id;
    }

    public DynamicMessage getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ComputationState{" +
                "id=" + id +
                '}';
    }
}
