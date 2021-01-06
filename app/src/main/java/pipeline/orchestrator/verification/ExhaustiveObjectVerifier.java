package pipeline.orchestrator.verification;

import com.google.common.base.Preconditions;
import pipeline.orchestrator.verification.errors.ErrorReport;
import pipeline.orchestrator.verification.errors.NotNullVerificationError;
import pipeline.orchestrator.verification.errors.PositiveVerificationError;

public class ExhaustiveObjectVerifier extends AbstractObjectVerifier {

    private ErrorReport.Builder currentBuilder;

    /**
     * Package private
     * Accesses should be made through the {@link Verifications} API
     */
    ExhaustiveObjectVerifier() {}

    public ErrorReport verify(Object object) {
        // Verify object while appending errors to the builder
        verifyTemplate(object);
        // Build report from builder
        ErrorReport report = currentBuilder.build();
        // Clear the builder for future calls
        currentBuilder = null;
        return report;
    }

    @Override
    protected void setContext() {
        // This method should be called in the beginning
        // of the verification and no error report should exist
        Preconditions.checkState(currentBuilder == null);
        currentBuilder = ErrorReport.newBuilder();
    }

    @Override
    protected void onNonPositiveField(String name, int value) {
        // When this method is called currentBuilder should be initialized
        Preconditions.checkState(currentBuilder != null);
        currentBuilder.append(PositiveVerificationError.forField(name, value));
    }

    @Override
    protected void onNullField(String name) {
        // When this method is called currentBuilder should be initialized
        Preconditions.checkState(currentBuilder != null);
        currentBuilder.append(NotNullVerificationError.forField(name));
    }
}
