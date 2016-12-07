package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.jboss.weld.annotated.slim.BaseAnnotated;

public abstract class UnbackedAnnotated extends BaseAnnotated {

    private final Set<Annotation> annotations;
    private final Set<Type> typeClosure;

    public UnbackedAnnotated(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations) {
        super(baseType);
        this.typeClosure = typeClosure;
        this.annotations = annotations;
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

    public Set<Type> getTypeClosure() {
        return typeClosure;
    }
}
