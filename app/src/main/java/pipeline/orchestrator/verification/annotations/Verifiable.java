package pipeline.orchestrator.verification.annotations;

import pipeline.orchestrator.verification.ObjectVerifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to set a class as verifiable
 * by the {@link ObjectVerifier}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Verifiable {
}
