package org.jboss.weld.metadata.cache;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.invoke.Invokable;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotation;

public class InvokableModel<T extends Annotation> extends AnnotationModel<T> {

    private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Collections.singleton(Invokable.class);

    /**
     * Constructor
     *
     * @param enhancedAnnotatedAnnotation
     */
    public InvokableModel(EnhancedAnnotation<T> enhancedAnnotatedAnnotation) {
        super(enhancedAnnotatedAnnotation);
    }

    @Override
    protected Set<Class<? extends Annotation>> getMetaAnnotationTypes() {
        return META_ANNOTATIONS;
    }
}
