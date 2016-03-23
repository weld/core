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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Singleton;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.bootstrap.events.InterceptorBuilderImpl;
import org.jboss.weld.bootstrap.events.builder.BeanBuilderImpl;
import org.jboss.weld.bootstrap.events.builder.BeanConfiguratorImpl;
import org.jboss.weld.environment.se.beans.InstanceManager;
import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.contexts.ThreadContext;
import org.jboss.weld.environment.se.contexts.interceptors.ActivateRequestScopeInterceptor;
import org.jboss.weld.environment.se.contexts.interceptors.ActivateThreadScopeInterceptor;
import org.jboss.weld.environment.se.threading.RunnableDecorator;
import org.jboss.weld.experimental.ExperimentalAfterBeanDiscovery;
import org.jboss.weld.experimental.InterceptorBuilder;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

/**
 * Explicitly registers all of the 'built-in' Java SE related beans and contexts.
 *
 * @author Peter Royle
 */
@Vetoed
public class WeldSEBeanRegistrant implements Extension {

    private ThreadContext threadContext;

    private List<BeanConfiguratorImpl<?>> beanConfigurators;

    private List<InterceptorBuilderImpl> interceptorBuilders;

    public void registerWeldSEBeans(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        if (ignoreEvent(event)) {
            return;
        }
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ParametersFactory.class, manager), ParametersFactory.class.getName());
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(InstanceManager.class, manager), InstanceManager.class.getName());
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(RunnableDecorator.class, manager), RunnableDecorator.class.getName());
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ActivateRequestScopeInterceptor.class, manager),
                ActivateRequestScopeInterceptor.class.getName());
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ActivateThreadScopeInterceptor.class, manager),
                ActivateThreadScopeInterceptor.class.getName());
    }

    public void registerWeldSEContexts(@Observes ExperimentalAfterBeanDiscovery event, BeanManager manager) {
        if (ignoreEvent(event)) {
            return;
        }

        final String contextId = BeanManagerProxy.unwrap(manager).getContextId();

        this.threadContext = new ThreadContext(contextId);
        event.addContext(threadContext);

        // Register WeldContainer as a singleton
        event.addBean().addType(WeldContainer.class).addQualifier(DefaultLiteral.INSTANCE).scope(Singleton.class)
                .produceWith(() -> WeldContainer.instance(contextId));

        // Process queued bean builders
        if (beanConfigurators != null) {
            for (BeanConfiguratorImpl<?> configurator : beanConfigurators) {
                event.addBean(new BeanBuilderImpl<>(configurator).build());
            }
        }
        if (interceptorBuilders != null) {
            for (InterceptorBuilder interceptorBuilder : interceptorBuilders) {
                event.addBean(interceptorBuilder.build());
            }
        }
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

    void setBeanConfigurators(List<BeanConfiguratorImpl<?>> beanConfigurators) {
        this.beanConfigurators = beanConfigurators;
    }

    void setInterceptorBuilders(List<InterceptorBuilderImpl> interceptorBuilders) {
        this.interceptorBuilders = interceptorBuilders;
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
}
