package org.jboss.weld.tests.interceptors.extension.annotation;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.Interceptors;

import org.jboss.weld.introspector.ForwardingAnnotatedType;

public class InterceptorsExtension implements Extension {

    void registerCustomInterceptor(@Observes ProcessAnnotatedType<SimpleBean> pat) {
        final AnnotatedType<SimpleBean> oldAnnotatedType = pat.getAnnotatedType();

        AnnotatedType<SimpleBean> modifiedSimpleAnnotatedType = new ForwardingAnnotatedType<SimpleBean>() {
            private final AnnotationLiteral<Interceptors> interceptorsAnnotation = new InterceptorsLiteral();

            @Override
            public AnnotatedType<SimpleBean> delegate() {
                return oldAnnotatedType;
            }

            @Override
            public Set<Annotation> getAnnotations() {
                Set<Annotation> annotations = new HashSet<Annotation>();
                annotations.add(interceptorsAnnotation);
                return annotations;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T extends Annotation> T getAnnotation(final Class<T> annType) {
                if (Interceptors.class.equals(annType)) {
                    return (T) interceptorsAnnotation;
                }
                return null;
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return Interceptors.class.equals(annotationType);
            }
        };

        pat.setAnnotatedType(modifiedSimpleAnnotatedType);

    }

}
