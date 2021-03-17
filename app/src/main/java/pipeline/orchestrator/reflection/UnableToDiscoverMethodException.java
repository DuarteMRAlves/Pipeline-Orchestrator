package pipeline.orchestrator.reflection;

import com.google.protobuf.Descriptors;
import pipeline.orchestrator.grpc.reflection.UnableToListServicesException;
import pipeline.orchestrator.grpc.reflection.UnableToLookupServiceException;

public class UnableToDiscoverMethodException extends Exception {

    private static final String BASE_MSG = "Unable to discover method: ";

    private static final String UNKNOWN_ERROR_MSG = "Unknown error";

    public UnableToDiscoverMethodException() {
        super(String.join("", BASE_MSG, UNKNOWN_ERROR_MSG));
    }

    public UnableToDiscoverMethodException(String error) {
        super(String.join("", BASE_MSG, error));
    }

    public UnableToDiscoverMethodException(String error, Throwable cause) {
        super(String.join("", BASE_MSG, error), cause);
    }

    static UnableToDiscoverMethodException fromWrongNumberOfServices(int expected, int found) {
        return new UnableToDiscoverMethodException(String.format(
                "Wrong number of services: %d expected but found %d",
                expected,
                found));
    }

    static UnableToDiscoverMethodException fromUnableToListServicesException(
            String authority,
            UnableToListServicesException exception) {
        return new UnableToDiscoverMethodException(
                String.format("Unable to list services at %s", authority),
                exception);
    }

    static UnableToDiscoverMethodException fromUnableToLookupServiceException(
            String authority,
            UnableToLookupServiceException exception) {
        return new UnableToDiscoverMethodException(
                String.format("Unable to lookup service at %s", authority),
                exception);
    }

    static UnableToDiscoverMethodException fromDescriptorsValidationException(
            Descriptors.DescriptorValidationException exception) {
        return new UnableToDiscoverMethodException(
                "Protobuf Descriptors Error",
                exception);
    }
}
