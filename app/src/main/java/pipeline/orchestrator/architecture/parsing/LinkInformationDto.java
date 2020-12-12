package pipeline.orchestrator.architecture.parsing;

import pipeline.orchestrator.verification.annotations.Verifiable;
import pipeline.orchestrator.verification.annotations.VerifyNotNull;

/**
 * Class to store the configuration of a link
 * Should be stored as and entry in a adjacency list
 * for the connections in the processing graph
 */
@Verifiable
public class LinkInformationDto {

    /**
     * Information for the source stage
     * {@link EndpointDto#field} represents the field of the message
     * from the source that should be sent through the link
     * Can be null if the whole message should be sent
     */
    @VerifyNotNull
    private EndpointDto source;

    /**
     * Information for the target stage
     * {@link EndpointDto#field} represents the field of the message
     * from the source that should be sent through the link
     * Can be null if the whole message should be sent
     */
    @VerifyNotNull
    private EndpointDto target;

    public EndpointDto getSource() {
        return source;
    }

    public void setSource(EndpointDto source) {
        this.source = source;
    }

    public EndpointDto getTarget() {
        return target;
    }

    public void setTarget(EndpointDto target) {
        this.target = target;
    }

    /**
     * Class representing the endpoint of a link
     * Can either represent a source or a target stage
     */
    @Verifiable
    public static class EndpointDto {

        // Stage identifier for the endpoint
        @VerifyNotNull
        private String stage;

        private String field;

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }
    }
}
