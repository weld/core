/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.event;

import static org.jboss.weld.logging.messages.EventMessage.INVALID_WITH_ANNOTATIONS;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import org.jboss.weld.bootstrap.events.AbstractContainerEvent;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * An implementation of {@link ObserverMethod} used for events delivered to extensions. An event can obtain an information about the
 * observer method and receiver used for event delivery using {@link AbstractContainerEvent#getObserverMethod()} or
 * {@link AbstractContainerEvent#getReceiver()}, respectively. The observer method does not require contexts to be active.
 *
 * @author Jozef Hartinger
 *
 */
public class ExtensionObserverMethodImpl<T, X> extends ObserverMethodImpl<T, X> {

    private final Container containerLifecycleEventDeliveryLock;
    private final Set<Class<? extends Annotation>> requiredTypeAnnotations;

    protected ExtensionObserverMethodImpl(EnhancedAnnotatedMethod<T, ? super X> observer, RIBean<X> declaringBean, BeanManagerImpl manager) {
        super(observer, declaringBean, manager);
        this.containerLifecycleEventDeliveryLock = Container.instance();
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
        if (!requiredTypeAnnotations.isEmpty()) {
            Class<?> rawObserverType = Reflections.getRawType(getObservedType());
            if (!rawObserverType.equals(ProcessAnnotatedType.class) && !rawObserverType.equals(ProcessSyntheticAnnotatedType.class)) {
                throw new DefinitionException(INVALID_WITH_ANNOTATIONS, this);
            }
        }
    }

    @Override
    protected MethodInjectionPoint<T, ? super X> initMethodInjectionPoint(EnhancedAnnotatedMethod<T, ? super X> observer, RIBean<X> declaringBean, BeanManagerImpl manager) {
        // use silent creation of injection points for ProcessInjectionPoint events not to be fired for extention observer methods
        return InjectionPointFactory.silentInstance().createMethodInjectionPoint(observer, declaringBean, declaringBean.getBeanClass(), true, manager);
    }

    @Override
    protected void preNotify(T event, Object receiver) {
        if (event instanceof AbstractContainerEvent) {
            setNotificationContext((AbstractContainerEvent) event, this, receiver);
        }
    }

    @Override
    protected void postNotify(T event, Object receiver) {
        if (event instanceof AbstractContainerEvent) {
            setNotificationContext((AbstractContainerEvent) event, null, null);
        }
    }

    private void setNotificationContext(AbstractContainerEvent event, ObserverMethod<?> observer, Object receiver) {
        event.setObserverMethod(observer);
        event.setReceiver(receiver);
    }

    @Override
    protected Object getReceiverIfExists() {
        return getReceiver(null);
    }

    /*
     * Contexts may not be active during notification of container lifecycle events. Therefore, we invoke the methods direcly on
     * an extension instance.
     */
    @Override
    protected Object getReceiver(CreationalContext<?> ctx) {
        return getDeclaringBean().create(null);
    }

    @Override
    protected void sendEvent(T event, Object receiver, CreationalContext<?> creationalContext) {
        synchronized (containerLifecycleEventDeliveryLock) {
            super.sendEvent(event, receiver, creationalContext);
        }
    }

    public Collection<Class<? extends Annotation>> getRequiredTypeAnnotations() {
        return requiredTypeAnnotations;
    }
}
