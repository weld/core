/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.events.AbstractAnnotatedTypeRegisteringEvent;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.ImmutableSet;

/**
 * An implementation of {@link ObserverMethod} used for events delivered to extensions.
 * The observer method does not require contexts to be active.
 *
 * @author Jozef Hartinger
 *
 */
public class ExtensionObserverMethodImpl<T, X> extends ObserverMethodImpl<T, X> {

    private final Container containerLifecycleEventDeliveryLock;
    private final Set<Class<? extends Annotation>> requiredTypeAnnotations;

    protected ExtensionObserverMethodImpl(EnhancedAnnotatedMethod<T, ? super X> observer, RIBean<X> declaringBean, BeanManagerImpl manager) {
        super(observer, declaringBean, manager);
        this.containerLifecycleEventDeliveryLock = Container.instance(manager);
        this.requiredTypeAnnotations = initRequiredTypeAnnotations(observer);
    }

    protected Set<Class<? extends Annotation>> initRequiredTypeAnnotations(EnhancedAnnotatedMethod<T, ? super X> observer) {
        EnhancedAnnotatedParameter<?, ? super X> eventParameter = observer.getEnhancedParameters(Observes.class).get(0);
        WithAnnotations annotation = eventParameter.getAnnotation(WithAnnotations.class);
        if (annotation != null) {
            return ImmutableSet.<Class<? extends Annotation>>copyOf(annotation.value());
        }
        return Collections.emptySet();
    }

    @Override
    protected void checkRequiredTypeAnnotations(EnhancedAnnotatedParameter<?, ?> eventParameter) {
        Class<?> rawObserverType = Reflections.getRawType(getObservedType());
        boolean isProcessAnnotatedType = rawObserverType.equals(ProcessAnnotatedType.class) || rawObserverType.equals(ProcessSyntheticAnnotatedType.class);
        if (!isProcessAnnotatedType && !requiredTypeAnnotations.isEmpty()) {
            throw EventLogger.LOG.invalidWithAnnotations(this);
        }
        if (isProcessAnnotatedType && requiredTypeAnnotations.isEmpty()) {
            Type[] typeArguments = eventParameter.getActualTypeArguments();
            if (typeArguments.length == 0 || Reflections.isUnboundedWildcard(typeArguments[0]) || Reflections.isUnboundedTypeVariable(typeArguments[0])) {
                EventLogger.LOG.unrestrictedProcessAnnotatedTypes(this);
            }
        }
    }

    @Override
    protected MethodInjectionPoint<T, ? super X> initMethodInjectionPoint(EnhancedAnnotatedMethod<T, ? super X> observer, RIBean<X> declaringBean, BeanManagerImpl manager) {
        // use silent creation of injection points for ProcessInjectionPoint events not to be fired for extension observer methods
        return InjectionPointFactory.silentInstance().createMethodInjectionPoint(observer, declaringBean, declaringBean.getBeanClass(), true, manager);
    }

    @Override
    protected void preNotify(T event, Object receiver) {
        if (event instanceof AbstractAnnotatedTypeRegisteringEvent) {
            setNotificationContext((AbstractAnnotatedTypeRegisteringEvent) event, this, receiver);
        }
    }

    @Override
    protected void postNotify(T event, Object receiver) {
        if (event instanceof AbstractAnnotatedTypeRegisteringEvent) {
            setNotificationContext((AbstractAnnotatedTypeRegisteringEvent) event, null, null);
        }
    }

    private void setNotificationContext(AbstractAnnotatedTypeRegisteringEvent event, ObserverMethod<?> observer, Object receiver) {
        event.setReceiver(receiver);
    }

    /*
     * Contexts may not be active during notification of container lifecycle events. Therefore, we invoke the methods directly on
     * an extension instance.
     */
    @Override
    protected Object getReceiver(CreationalContext<X> ctx) {
        return getDeclaringBean().create(null);
    }

    @Override
    protected void sendEvent(T event, Object receiver, CreationalContext<?> creationalContext) {
        synchronized (containerLifecycleEventDeliveryLock) {
            super.sendEvent(event, receiver, creationalContext);
        }
    }

    public Collection<Class<? extends Annotation>> getRequiredAnnotations() {
        return requiredTypeAnnotations;
    }
}
