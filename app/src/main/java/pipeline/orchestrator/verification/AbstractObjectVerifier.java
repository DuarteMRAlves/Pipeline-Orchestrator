package pipeline.orchestrator.verification;

import org.reflections.ReflectionUtils;
import pipeline.orchestrator.verification.annotations.Verifiable;
import pipeline.orchestrator.verification.annotations.VerifyIterable;
import pipeline.orchestrator.verification.annotations.VerifyNotNull;
import pipeline.orchestrator.verification.annotations.VerifyPositive;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Abstract class that verifies an object
 * Implements the template design pattern
 * leaving the actions of handling verification
 * errors to the child classes
 */
public abstract class AbstractObjectVerifier {

    private final Set<FieldIdentifier> verifiedFields = new HashSet<>();

    /**
     * Package private
     * Accesses should be made through the {@link Verifications} API
     */
    AbstractObjectVerifier() {}

    /**
     * Check if object meets all desired predicates
     * @param object to verify
     */
    protected final void verifyTemplate(Object object) {
        setContext();
        verifyObject(object);
    }

    /**
     * Method to be called when starting the verification of
     * an object so that a child class can initialize any
     * necessary structure
     */
    protected abstract void setContext();

    /**
     * Method to be called when a non positive field is found
     * @param name name of the field
     * @param value value for the field
     */
    protected abstract void onNonPositiveField(String name, int value);

    /**
     * Method to be called when a null field is found
     * @param name name of the field
     */
    protected abstract void onNullField(String name);

    /**
     * Verifies a given object for the desired predicates
     * Checks if the annotations conditions are correct
     * and then recursively verify fields
     * @param object object to verify
     */
    private void verifyObject(Object object) {
        // Check if field class is verifiable to avoid reflective accesses
        // to unwanted fields
        if (!object.getClass().isAnnotationPresent(Verifiable.class)) {
            return;
        }

        verifyAnnotation(object, VerifyPositive.class, this::verifyPositive);
        verifyAnnotation(object, VerifyNotNull.class, this::verifyNotNull);
        verifyIterables(object);
        ReflectionUtils.getAllFields(
                object.getClass())
                .forEach(field -> verifyField(object, field));
    }

    /**
     * Verify a field of a given object
     * @param object object under analysis
     * @param field field of the object to analyse
     */
    private void verifyField(Object object, Field field) {
        // Check if field already verified for classes that point to themselves
        FieldIdentifier fieldIdentifier = new FieldIdentifier(object, field);
        if (verifiedFields.contains(fieldIdentifier)) {
            return;
        }
        verifiedFields.add(fieldIdentifier);

        try {
            field.setAccessible(true);
            Optional.ofNullable(field.get(object))
                    .ifPresent(this::verifyObject);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private void verifyAnnotation(
            Object object,
            Class<? extends Annotation> annotation,
            BiConsumer<Object, Field> verifyField) {

        ReflectionUtils.getAllFields(
                object.getClass(),
                ReflectionUtils.withAnnotation(annotation))
                .forEach(field -> verifyField.accept(object, field));
    }

    private void verifyPositive(Object object, Field field) {
        int value;
        try {
            field.setAccessible(true);
            value = field.getInt(object);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }

        if (value <= 0)
            onNonPositiveField(field.getName(), value);
    }

    private void verifyNotNull(Object object, Field field) {
        Object value = getFieldValue(object, field);

        if (value == null)
            onNullField(field.getName());
    }

    private void verifyIterables(Object object) {
        ReflectionUtils.getAllFields(
                object.getClass(),
                ReflectionUtils.withAnnotation(VerifyIterable.class))
                .forEach(field -> verifyIterable(object, field));
    }

    private void verifyIterable(Object object, Field field) {
        Object value = getFieldValue(object, field);

        if (!(value instanceof Iterable)) {
            return;
        }

        Iterable<?> iterable = (Iterable<?>) value;
        for (Object innerObject : iterable) {
            verifyObject(innerObject);
        }
    }

    private Object getFieldValue(Object object, Field field) {
        Object value;
        try {
            field.setAccessible(true);
            value = field.get(object);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return value;
    }

    private static class FieldIdentifier {
        private final Object parent;
        private final Field field;

        private FieldIdentifier(Object parent, Field field) {
            this.parent = parent;
            this.field = field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldIdentifier that = (FieldIdentifier) o;
            return Objects.equals(parent, that.parent) && field.equals(that.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, field);
        }
    }
}
