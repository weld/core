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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.contexts.ThreadContext;
import org.jboss.weld.environment.se.contexts.activators.ActivateThreadScopeInterceptor;
import org.jboss.weld.environment.se.threading.RunnableDecorator;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.util.annotated.VetoedSuppressedAnnotatedType;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Explicitly registers all of the 'built-in' Java SE related beans and contexts.
 *
 * @author Peter Royle
 * @author Martin Kouba
 */
@Vetoed
public class WeldSEBeanRegistrant implements Extension {

    private ThreadContext threadContext;

    public void registerWeldSEBeans(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        if (ignoreEvent(event)) {
            return;
        }
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(ParametersFactory.class, manager));
        event.addAnnotatedType(VetoedSuppressedAnnotatedType.from(RunnableDecorator.class, manager));
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

    /**
     * Returns <tt>true</tt> if the specified event is not an instance of {@link AbstractContainerEvent}, i.e. was thrown by other CDI implementation than Weld.
     */
    private static boolean ignoreEvent(Object event) {
        return !(event instanceof AbstractContainerEvent);
    }

    public ThreadContext getThreadContext() {
        return threadContext;
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

}