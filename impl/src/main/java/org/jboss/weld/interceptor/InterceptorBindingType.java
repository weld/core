package org.jboss.weld.interceptor;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.InterceptorBindingModel;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class InterceptorBindingType {

    private BeanManagerImpl beanManager;
    private Annotation annotation;

    public InterceptorBindingType(BeanManagerImpl beanManager, Annotation annotation) {
        this.beanManager = beanManager;
        this.annotation = annotation;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public static Set<Annotation> unwrap(Set<InterceptorBindingType> interceptorBindingTypes) {
        HashSet<Annotation> annotations = new HashSet<Annotation>();
        for (InterceptorBindingType interceptorBindingType : interceptorBindingTypes) {
            annotations.add(interceptorBindingType.getAnnotation());
        }
        return annotations;
    }

    public Class<? extends Annotation> annotationType() {
        return getAnnotation().annotationType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterceptorBindingType that = (InterceptorBindingType) o;

        MetaAnnotationStore metaAnnotationStore = beanManager.getMetaAnnotationStore();
        InterceptorBindingModel<? extends Annotation> interceptorBindingModel = metaAnnotationStore.getInterceptorBindingModel(annotation.annotationType());
        return interceptorBindingModel.isEqual(getAnnotation(), that.getAnnotation());
    }

    @Override
    public int hashCode() {
        return annotationType().hashCode();
    }
}
