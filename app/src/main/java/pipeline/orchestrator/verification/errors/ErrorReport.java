package pipeline.orchestrator.verification.errors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to store the list of errors found while
 * verifying the properties of an object
 */
public class ErrorReport {

    private static final Map<Class<? extends VerificationError>, String> SECTION_HEADERS =
            ImmutableMap.of(
                    NotNullVerificationError.class, "Not Null Verification Errors",
                    PositiveVerificationError.class, "Positive Verification Errors");

    public static final String NO_ERRORS_DESCRIPTION = "ERROR REPORT: No errors fourd.\n";

    private final ImmutableList<VerificationError> errors;

    private ErrorReport(ImmutableList<VerificationError> errors) {
        this.errors = errors;
    }

    public ImmutableList<VerificationError> getErrors() { return errors; }

    public boolean hasErrors() { return !errors.isEmpty(); }

    public String summarize() {
        if (errors.isEmpty()) {
            return NO_ERRORS_DESCRIPTION;
        }

        StringBuilder sb = new StringBuilder("ERROR REPORT:\n\n");

        errors.stream()
                // Group errors by class
                .collect(Collectors.groupingBy(VerificationError::getClass))
                // Write each class as a section
                .forEach((cls, clsErrors) -> summarizeErrorClass(sb, cls, clsErrors));

        return sb.toString();
    }

    private void summarizeErrorClass(
            StringBuilder sb,
            Class<? extends VerificationError> errorClass,
            List<VerificationError> classErrors) {

        // Sections with no errors are not printed
        // Should not happen since all found groups should
        // have at least on member
        if (classErrors.isEmpty()) { return; }

        String header = SECTION_HEADERS.getOrDefault(errorClass, "Other Errors");

        sb.append("  ")
                .append(header)
                .append(":\n\n");

        // Append messages with tab size 1
        classErrors.forEach(error -> error.summarize(sb, 1));
        sb.append('\n');
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private final List<VerificationError> errors = Lists.newArrayList();

        public Builder append(VerificationError error) {
            errors.add(error);
            return this;
        }

        public ErrorReport build() {
            return new ErrorReport(ImmutableList.copyOf(errors));
        }
    }
}
