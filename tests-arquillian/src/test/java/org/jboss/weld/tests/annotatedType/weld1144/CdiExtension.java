package org.jboss.weld.tests.annotatedType.weld1144;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

public class CdiExtension implements Extension {

    @SuppressWarnings("unchecked")
    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedType) {

        final AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();

        // Replace each field with (theoretically) an identical field
        final Set<AnnotatedField<? super T>> processedFields = new HashSet<AnnotatedField<? super T>>();

        for (final AnnotatedField<? super T> annotatedField : annotatedType.getFields()) {
            // Use 'annotatedField' instead of 'new AnnotatedField' and the code works:
            //            processedFields.add( annotatedField ); if ( true ) { continue; }
            processedFields.add(new MyAnnotatedField<T>(annotatedField));
        }

        processAnnotatedType.setAnnotatedType(new MyAnnotatedType<T>(processedFields, annotatedType));
    }

    private class MyAnnotatedField<T> implements AnnotatedField<T> {
        private final AnnotatedField<? super T> annotatedField;

        public MyAnnotatedField(AnnotatedField<? super T> annotatedField) {
            this.annotatedField = annotatedField;
        }

        public Type getBaseType() {
            return annotatedField.getBaseType();
        }

        public Set<Annotation> getAnnotations() {
            return annotatedField.getAnnotations();
        }

        public <X extends Annotation> X getAnnotation(Class<X> clazz) {
            return annotatedField.getAnnotation(clazz);
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
            return annotatedField.isAnnotationPresent(clazz);
        }

        public AnnotatedType<T> getDeclaringType() {
            return (AnnotatedType<T>) annotatedField.getDeclaringType();
        }

        public boolean isStatic() {
            return annotatedField.isStatic();
        }

        public Set<Type> getTypeClosure() {
            return annotatedField.getTypeClosure();
        }

        public Field getJavaMember() {
            return annotatedField.getJavaMember();
        }
    }

    private class MyAnnotatedType<T> implements AnnotatedType<T> {
        private final Set<AnnotatedField<? super T>> processedFields;
        private final AnnotatedType<T> annotatedType;

        public MyAnnotatedType(Set<AnnotatedField<? super T>> processedFields, AnnotatedType<T> annotatedType) {
            this.processedFields = processedFields;
            this.annotatedType = annotatedType;
        }

        public Set<AnnotatedField<? super T>> getFields() {
            return processedFields;
        }

        public Type getBaseType() {
            return annotatedType.getBaseType();
        }

        public <X extends Annotation> X getAnnotation(Class<X> clazz) {
            return annotatedType.getAnnotation(clazz);
        }

        public Set<Annotation> getAnnotations() {
            return annotatedType.getAnnotations();
        }

        public Set<Type> getTypeClosure() {
            return annotatedType.getTypeClosure();
        }

        public boolean isAnnotationPresent(Class<? extends Annotation> arg0) {
            return annotatedType.isAnnotationPresent(arg0);
        }

        public Set<AnnotatedConstructor<T>> getConstructors() {
            return annotatedType.getConstructors();
        }

        public Class<T> getJavaClass() {
            return annotatedType.getJavaClass();
        }

        public Set<AnnotatedMethod<? super T>> getMethods() {
            return annotatedType.getMethods();
        }
    }
}