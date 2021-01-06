package pipeline.orchestrator.verification.errors;

/**
 * Abstract verification error
 * Describes a violation of the required properties
 * for an object
 */
public interface VerificationError {

    /**
     * Summarizes a description of the error
     * @param sb string builder to append the error to
     * @param tab tab size to use when writing the description
     */
    void summarize(StringBuilder sb, int tab);
}
