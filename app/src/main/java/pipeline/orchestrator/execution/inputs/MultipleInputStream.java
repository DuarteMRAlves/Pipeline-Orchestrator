package pipeline.orchestrator.execution.inputs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import pipeline.orchestrator.execution.ComputationState;
import pipeline.orchestrator.execution.Link;
import pipeline.orchestrator.grpc.messages.DynamicMessageMerger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MultipleInputStream implements StageInputStream {

    private final DynamicMessageMerger merger;
    private final ImmutableMap<String, Link> inputs;

    // Id of the next message to be created
    private int currentId = 0;

    // Map with most recent states for all inputs
    private final Map<String, ComputationState> mostRecentStates = new HashMap<>();

    MultipleInputStream(
            Descriptors.Descriptor descriptor,
            ImmutableSetMultimap<String, Link> inputs) {
        this.inputs = ImmutableMap.copyOf(inputs.entries());
        merger = DynamicMessageMerger.newBuilder()
                .forDescriptor(descriptor)
                .build();
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    public ComputationState get() {
        ImmutableMap.Builder<String, DynamicMessage> builder =
                ImmutableMap.builder();

        for (String fieldName : inputs.keySet()) {
            Optional<ComputationState> optionalComputationState =
                    tryGetFirstWithId(fieldName, currentId);

            if (optionalComputationState.isPresent()) {
                ComputationState state = optionalComputationState.get();
                // We can use this message since is from the state with
                // the current id
                if (state.getId() == currentId) {
                    builder.put(fieldName, state.getMessage());
                }
                // The first message from the given link has a
                // higher id and so we must return a state with that id or higher
                else {
                    currentId = state.getId();
                    return get();
                }
            }
            else {
                return null;
            }
        }
        // Increment Id so that the next message
        // to be delivered has at least the next id
        return ComputationState.from(
                currentId++,
                merger.merge(builder.build()));
    }

    /**
     * Returns the first computation state with an equal or higher id
     * First checks if there is already a state in the most recent states
     * @param fieldName the field name for the link to use
     * @param id id to compare the states to
     * @return a computational state with a higher or equal id to the provided
     */
    private Optional<ComputationState> tryGetFirstWithId(String fieldName, int id) {
        return Optional.ofNullable(mostRecentStates.get(fieldName))
                .filter(computationState -> computationState.getId() >= id)
                .or(() -> takeLinkUntilId(fieldName, id));
    }

    /**
     * Removes states from the link until a higher or equal id is found
     * and returns that state updating the most recent states for that field name
     * @param fieldName the field name for the link to use
     * @param id id to test
     * @return the first computational state with a higher or equal id
     *         provided by the link
     */
    private Optional<ComputationState> takeLinkUntilId(String fieldName, int id) {
        try {
            ComputationState linkState = inputs.get(fieldName).takeUntil(
                    computationState -> computationState.getId() >= id);
            mostRecentStates.put(fieldName, linkState);
            return Optional.ofNullable(linkState);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
