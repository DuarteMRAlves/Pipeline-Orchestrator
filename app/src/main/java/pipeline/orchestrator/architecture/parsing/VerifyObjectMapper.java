package pipeline.orchestrator.architecture.parsing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pipeline.orchestrator.verification.ObjectVerifier;
import pipeline.orchestrator.verification.Verifications;

import java.io.File;
import java.io.IOException;
/**
 * Wrapper class to verify an object fields for predicates
 * Uses the decorator pattern calling the method from ObjectMapper
 * and then checking the result
 */
public class VerifyObjectMapper extends ObjectMapper {

    public VerifyObjectMapper(JsonFactory jf) {
        super(jf);
    }

    @Override
    public <T> T readValue(String content, Class<T> valueType)
            throws JsonProcessingException {

        T value = super.readValue(content, valueType);
        Verifications.verify(value);
        return value;
    }

    @Override
    public <T> T readValue(File src, Class<T> valueType)
            throws IOException {

        T value = super.readValue(src, valueType);
        Verifications.verify(value);
        return value;
    }
}
