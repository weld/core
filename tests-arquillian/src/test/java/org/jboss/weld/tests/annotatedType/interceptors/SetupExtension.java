package org.jboss.weld.tests.annotatedType.interceptors;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.util.Function;
import org.jboss.weld.util.annotated.ForwardingAnnotatedMethod;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

public class SetupExtension implements Extension {

    void registerAdditionalBox(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        final AnnotatedType<Box> original = manager.createAnnotatedType(Box.class);

        /*
         * We wrap the annotated type. As a result it does not contain the interceptor binding but it does contain a new qualifier to distinguish it from the
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
                return Collections.<Annotation>singleton(Additional.Literal.INSTANCE);
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                return getAnnotation(annotationType) != null;
            }

            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<AnnotatedMethod<? super Box>> getMethods() {
                Set<AnnotatedMethod<? super Box>> methods = new HashSet<>();
                Set<AnnotatedMethod<? super Box>> annotatedMethods = delegate().getMethods();
                for (final AnnotatedMethod<? super Box> annotatedMethod : annotatedMethods) {
                    methods.add(new NoAnnotationMethodWrapper() {
                        protected AnnotatedMethod<? super Box> delegate() {
                            return annotatedMethod;
                        }
                    });
                }
                return methods;
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

    private static class MethodWrappingFunction<T> implements Function<AnnotatedMethod<T>, AnnotatedMethod<T>> {
        @Override
        public AnnotatedMethod<T> apply(final AnnotatedMethod<T> input) {
            return new NoAnnotationMethodWrapper<T>() {

                @Override
                protected AnnotatedMethod<T> delegate() {
                    return input;
                }
            };
        }
    }
}
