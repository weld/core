package org.jboss.weld.annotated.backed;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

public class BackedAnnotatedParameter<X> extends BackedAnnotated implements AnnotatedParameter<X> {

    public static <X> AnnotatedParameter<X> of(Type baseType, Set<Annotation> annotations, int position, AnnotatedCallable<X> declaringCallable) {
        return new BackedAnnotatedParameter<X>(baseType, annotations, position, declaringCallable);
    }

    private final int position;
    private final AnnotatedCallable<X> declaringCallable;
    private final Set<Annotation> annotations;

    public BackedAnnotatedParameter(Type baseType, Set<Annotation> annotations, int position, AnnotatedCallable<X> declaringCallable) {
        super(baseType);
        this.position = position;
        this.declaringCallable = declaringCallable;
        this.annotations = annotations;
    }

    public int getPosition() {
        return position;
    }

    public AnnotatedCallable<X> getDeclaringCallable() {
        return declaringCallable;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationType)) {
                return cast(annotation);
            }
        }
        return null;
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

}
