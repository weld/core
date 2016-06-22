/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.se;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Singleton;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.environment.se.beans.InstanceManager;
import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.contexts.ThreadContext;
import org.jboss.weld.environment.se.contexts.activators.ActivateRequestScopeInterceptor;
import org.jboss.weld.environment.se.contexts.activators.ActivateThreadScopeInterceptor;
import org.jboss.weld.environment.se.contexts.interceptors.ActivateThreadScope;
import org.jboss.weld.environment.se.logging.WeldSELogger;
import org.jboss.weld.environment.se.threading.RunnableDecorator;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

import com.google.common.collect.ImmutableSet;

/**
 * Explicitly registers all of the 'built-in' Java SE related beans and contexts.
 *
 * @author Peter Royle
 */
@Vetoed
public class WeldSEBeanRegistrant implements Extension {

    private ThreadContext threadContext;

    public void registerWeldSEBeans(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        if (ignoreEvent(event)) {
            return;
        }
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ParametersFactory.class, manager));
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(InstanceManager.class, manager));
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(RunnableDecorator.class, manager));
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ActivateRequestScopeInterceptor.class, manager));
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ActivateThreadScopeInterceptor.class, manager));
    }

    public void registerWeldSEContexts(@Observes AfterBeanDiscovery event, BeanManager manager) {
        if (ignoreEvent(event)) {
            return;
        }

        final String contextId = BeanManagerProxy.unwrap(manager).getContextId();

        this.threadContext = new ThreadContext(contextId);
        event.addContext(threadContext);

        // Register WeldContainer as a singleton
        event.addBean(new WeldContainerBean(contextId));
    }

    <T> void replaceDeprecatedActivator(@Observes @WithAnnotations(ActivateThreadScope.class) ProcessAnnotatedType<T> event) {
        final AnnotatedType<T> annotatedType = event.getAnnotatedType();
        WeldSELogger.LOG.deprecatedActivatorAnnotationUsed(ActivateThreadScope.class.getName(),
                org.jboss.weld.environment.se.contexts.activators.ActivateThreadScope.class.getName(), annotatedType.getJavaClass());
        event.setAnnotatedType(new AnnotatedTypeWrapper<>(annotatedType));
    }

    /**
     * Returns <tt>true</tt> if the specified event is not an instance of {@link AbstractContainerEvent}, i.e. was thrown by other CDI implementation than Weld.
     */
    private static boolean ignoreEvent(Object event) {
        return !(event instanceof AbstractContainerEvent);
    }

    public ThreadContext getThreadContext() {
        return threadContext;
    }

    private static class VetoedSuppressedAnnotatedType<T> extends ForwardingAnnotatedType<T> {

        static <T> VetoedSuppressedAnnotatedType<T> from(Class<T> clazz, BeanManager beanManager) {
            return new VetoedSuppressedAnnotatedType<T>(beanManager.createAnnotatedType(clazz));
        }

        private final AnnotatedType<T> annotatedType;

        public VetoedSuppressedAnnotatedType(AnnotatedType<T> annotatedType) {
            this.annotatedType = annotatedType;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            if (annotationType == Vetoed.class) {
                return null;
            }
            return annotatedType.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            Set<Annotation> annotations = new HashSet<Annotation>();
            for (Annotation a : annotatedType.getAnnotations()) {
                if (a.annotationType() != Vetoed.class) {
                    annotations.add(a);
                }
            }
            return annotations;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            if (annotationType == Vetoed.class) {
                return false;
            }
            return annotatedType.isAnnotationPresent(annotationType);
        }

        @Override
        public AnnotatedType<T> delegate() {
            return annotatedType;
        }

    }

    private static class WeldContainerBean implements Bean<WeldContainer> {

        private final String contextId;

        private WeldContainerBean(String contextId) {
            this.contextId = contextId;
        }

        @Override
        public WeldContainer create(CreationalContext<WeldContainer> creationalContext) {
            return WeldContainer.instance(contextId);
        }

        @Override
        public void destroy(WeldContainer instance, CreationalContext<WeldContainer> creationalContext) {
        }

        @Override
        public Set<Type> getTypes() {
            return ImmutableSet.<Type> of(WeldContainer.class, Object.class);
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return ImmutableSet.of(AnyLiteral.INSTANCE, DefaultLiteral.INSTANCE);
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Singleton.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public Class<?> getBeanClass() {
            return WeldSEBeanRegistrant.class;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        @Override
        public boolean isNullable() {
            return false;
        }

    }

    static class AnnotatedTypeWrapper<X> implements AnnotatedType<X> {

        private final AnnotatedType<X> delegate;
        private final ImmutableSet<AnnotatedMethod<? super X>> annotatedMethods;
        private final ImmutableSet<Annotation> annotations;

        public AnnotatedTypeWrapper(AnnotatedType<X> annotatedType) {
            this.delegate = annotatedType;
            this.annotatedMethods = replaceDeprecatedAnnotatedMethods(delegate.getMethods());
            this.annotations = replaceDeprecatedAnnotations(delegate.getAnnotations());
        }

        @Override
        public Class<X> getJavaClass() {
            return delegate.getJavaClass();
        }

        @Override
        public Set<AnnotatedConstructor<X>> getConstructors() {
            return delegate.getConstructors();
        }

        @Override
        public Set<AnnotatedMethod<? super X>> getMethods() {
            return annotatedMethods;
        }

        @Override
        public Set<AnnotatedField<? super X>> getFields() {
            return delegate.getFields();
        }

        @Override
        public Type getBaseType() {
            return delegate.getBaseType();
        }

        @Override
        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationType)) {
                    return annotationType.cast(annotation);
                }
            }
            return null;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return getAnnotation(annotationType) != null;
        }

        private ImmutableSet<AnnotatedMethod<? super X>> replaceDeprecatedAnnotatedMethods(Set<AnnotatedMethod<? super X>> delegateMethods) {
            ImmutableSet.Builder<AnnotatedMethod<? super X>> annotatedMethodBuilder = ImmutableSet.builder();
            for (final AnnotatedMethod<? super X> originalMethod : delegateMethods) {
                if (originalMethod.isAnnotationPresent(ActivateThreadScope.class)) {
                    annotatedMethodBuilder.add(new AnnotatedMethodWrapper<>(originalMethod));
                } else {
                    annotatedMethodBuilder.add(originalMethod);
                }
            }
            return annotatedMethodBuilder.build();
        }

        private ImmutableSet<Annotation> replaceDeprecatedAnnotations(Set<Annotation> delegateAnnotations) {
            ImmutableSet.Builder<Annotation> annotationBuilder = ImmutableSet.builder();
            Iterator<Annotation> it = delegateAnnotations.iterator();
            while (it.hasNext()) {
                Annotation annotation = it.next();
                if (annotation.annotationType().equals(ActivateThreadScope.class)) {
                    annotationBuilder.add(org.jboss.weld.environment.se.contexts.activators.ActivateThreadScope.Literal.INSTANCE);
                } else {
                    annotationBuilder.add(annotation);
                }

            }
            return annotationBuilder.build();
        }

    }

    static class AnnotatedMethodWrapper<X> implements AnnotatedMethod<X> {

        private final AnnotatedMethod<X> delegate;
        private final ImmutableSet<Annotation> annotations;

        public AnnotatedMethodWrapper(AnnotatedMethod<X> annotatedMethod) {
            this.delegate = annotatedMethod;
            this.annotations = replaceDeprecatedAnnotations(delegate.getAnnotations());
        }

        @Override
        public Method getJavaMember() {
            return delegate.getJavaMember();
        }

        @Override
        public List<AnnotatedParameter<X>> getParameters() {
            return delegate.getParameters();
        }

        @Override
        public boolean isStatic() {
            return delegate.isStatic();
        }

        @Override
        public AnnotatedType<X> getDeclaringType() {
            return delegate.getDeclaringType();
        }

        @Override
        public Type getBaseType() {
            return delegate.getBaseType();
        }

        @Override
        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationType)) {
                    return annotationType.cast(annotation);
                }
            }
            return null;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations;
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return getAnnotation(annotationType) != null;
        }

        private ImmutableSet<Annotation> replaceDeprecatedAnnotations(Set<Annotation> delegateAnnotations) {
            ImmutableSet.Builder<Annotation> annotationBuilder = ImmutableSet.builder();
            Iterator<Annotation> it = delegateAnnotations.iterator();
            while (it.hasNext()) {
                Annotation annotation = it.next();
                if (annotation.annotationType().equals(ActivateThreadScope.class)) {
                    annotationBuilder.add(org.jboss.weld.environment.se.contexts.activators.ActivateThreadScope.Literal.INSTANCE);
                } else {
                    annotationBuilder.add(annotation);
                }

            }
            return annotationBuilder.build();
        }
    }

}