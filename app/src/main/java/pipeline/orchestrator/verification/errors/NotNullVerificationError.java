package pipeline.orchestrator.verification.errors;

import com.google.common.base.Strings;

public class NotNullVerificationError implements VerificationError {

    private static final String MESSAGE = "Field %s is null.";

    private final String field;

    private NotNullVerificationError(String field) {
        this.field = field;
    }

    public static NotNullVerificationError forField(String field) {
        return new NotNullVerificationError(field);
    }

    public String getField() {
        return field;
    }

    @Override
    public void summarize(StringBuilder sb, int tab) {
        sb.append(Strings.repeat("\t", tab))
                .append(String.format(MESSAGE, field))
                .append('\n');
    }

}
