package pipeline.orchestrator.verification.errors;

import com.google.common.base.Strings;

public class PositiveVerificationError implements VerificationError {

    private static final String MESSAGE = "Field %s is not positive. Found value %d.";

    private final String field;
    private final int value;

    private PositiveVerificationError(String field, int value) {
        this.field = field;
        this.value = value;
    }

    public static PositiveVerificationError forField(String field, int value) {
        return new PositiveVerificationError(field, value);
    }

    public String getField() {
        return field;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void summarize(StringBuilder sb, int tab) {
        sb.append(Strings.repeat("\t", tab))
                .append(String.format(MESSAGE, field, value))
                .append('\n');
    }

}
