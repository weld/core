package org.jboss.weld.tests.annotatedType.interceptors;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

import org.jboss.weld.util.annotated.ForwardingAnnotatedMethod;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;
import org.jboss.weld.util.collections.ImmutableSet;

public class SetupExtension implements Extension {

    void registerAdditionalBox(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        final AnnotatedType<Box> original = manager.createAnnotatedType(Box.class);

        /*
         * We wrap the annotated type. As a result it does not contain the interceptor binding but it does contain a new
         * qualifier to distinguish it from the
         * default annotated type for Box.class
         */
        AnnotatedType<Box> modifiedType = new ForwardingAnnotatedType<Box>() {
            @Override
            public AnnotatedType<Box> delegate() {
                return original;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                if (Additional.class.equals(annotationType)) {
                    return (A) Additional.Literal.INSTANCE;
                }
                return null;
            }

            @Override
            public Set<Annotation> getAnnotations() {
                return Collections.<Annotation> singleton(Additional.Literal.INSTANCE);
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return getAnnotation(annotationType) != null;
            }

            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<AnnotatedMethod<? super Box>> getMethods() {
                // We don't use stream API due to odd generics issues
                ImmutableSet.Builder builder = ImmutableSet.builder();
                Set<AnnotatedMethod<? super Box>> annotatedMethods = delegate().getMethods();
                for (final AnnotatedMethod<? super Box> annotatedMethod : annotatedMethods) {
                    builder.add(new NoAnnotationMethodWrapper() {
                        protected AnnotatedMethod<? super Box> delegate() {
                            return annotatedMethod;
                        }
                    });
                }
                return builder.build();
            }
        };

        event.addAnnotatedType(modifiedType, "additionalBox");
    }

    private abstract static class NoAnnotationMethodWrapper<T> extends ForwardingAnnotatedMethod<T> {

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return null;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return Collections.emptySet();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return false;
        }

    }

}
