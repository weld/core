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

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bootstrap.events.NotificationListener;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint.MethodInjectionPointType;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * An implementation of {@link ObserverMethod} used for events delivered to extensions.
 * The observer method does not require contexts to be active.
 *
 * @author Jozef Hartinger
 *
 */
public class ExtensionObserverMethodImpl<T, X> extends ObserverMethodImpl<T, X>
        implements ContainerLifecycleEventObserverMethod<T> {

    private final Container containerLifecycleEventDeliveryLock;
    private final Set<Class<? extends Annotation>> requiredTypeAnnotations;
    private volatile Set<Class<? extends Annotation>> requiredScopeTypeAnnotations;

    protected ExtensionObserverMethodImpl(EnhancedAnnotatedMethod<T, ? super X> observer, RIBean<X> declaringBean,
            BeanManagerImpl manager, boolean isAsync) {
        super(observer, declaringBean, manager, isAsync);
        this.containerLifecycleEventDeliveryLock = Container.instance(manager);
        this.requiredTypeAnnotations = initRequiredTypeAnnotations(observer);
    }

    protected Set<Class<? extends Annotation>> initRequiredTypeAnnotations(EnhancedAnnotatedMethod<T, ? super X> observer) {
        EnhancedAnnotatedParameter<?, ? super X> eventParameter = getEventParameter(observer);
        WithAnnotations annotation = eventParameter.getAnnotation(WithAnnotations.class);
        if (annotation != null) {
            return ImmutableSet.<Class<? extends Annotation>> of(annotation.value());
        }
        return Collections.emptySet();
    }

    @Override
    protected <Y> void checkRequiredTypeAnnotations(EnhancedAnnotatedParameter<?, ?> eventParameter,
            EnhancedAnnotatedMethod<T, Y> annotated) {
        Class<?> rawObserverType = Reflections.getRawType(getObservedType());
        boolean isProcessAnnotatedType = rawObserverType.equals(ProcessAnnotatedType.class)
                || rawObserverType.equals(ProcessSyntheticAnnotatedType.class);
        if (!isProcessAnnotatedType && !requiredTypeAnnotations.isEmpty()) {
            throw EventLogger.LOG
                    .invalidWithAnnotations(this,
                            Formats.formatAsStackTraceElement(eventParameter.getDeclaringEnhancedCallable().getJavaMember()));
        }
        if (isProcessAnnotatedType && requiredTypeAnnotations.isEmpty()) {
            Type[] typeArguments = eventParameter.getActualTypeArguments();
            if ((typeArguments.length == 0 || Reflections.isUnboundedWildcard(typeArguments[0])
                    || Reflections.isUnboundedTypeVariable(typeArguments[0])) &&
            // LiteExtensionTranslator is an exception because it is the only way to implement build compatible extensions via portable extensions
            // Not that we use hardcoded String because we want to avoid circular dependencies between weld-core-impl and weld-lite-extension-translator
                    !annotated.getJavaMember().getDeclaringClass().getName()
                            .equals("org.jboss.weld.lite.extension.translator.LiteExtensionTranslator")) {
                EventLogger.LOG.unrestrictedProcessAnnotatedTypes(this);
            }
        }
    }

    @Override
    protected MethodInjectionPoint<T, ? super X> initMethodInjectionPoint(EnhancedAnnotatedMethod<T, ? super X> observer,
            RIBean<X> declaringBean, BeanManagerImpl manager) {
        // use silent creation of injection points for ProcessInjectionPoint events not to be fired for extension observer methods
        return InjectionPointFactory.silentInstance().createMethodInjectionPoint(MethodInjectionPointType.OBSERVER, observer,
                declaringBean, declaringBean.getBeanClass(), SPECIAL_PARAM_MARKERS, manager);
    }

    @Override
    protected void preNotify(T event, Object receiver) {
        if (event instanceof NotificationListener) {
            NotificationListener.class.cast(event).preNotify((Extension) receiver);
        }
    }

    @Override
    protected void postNotify(T event, Object receiver) {
        if (event instanceof NotificationListener) {
            NotificationListener.class.cast(event).postNotify((Extension) receiver);
        }
    }

    /*
     * Contexts may not be active during notification of container lifecycle events. Therefore, we invoke the methods directly
     * on
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

    protected String createTypeId(RIBean<?> declaringBean) {
        if (declaringBean instanceof ExtensionBean) {
            ExtensionBean<?> extensionBean = (ExtensionBean<?>) declaringBean;
            return extensionBean.getAnnotatedType().getIdentifier().asString();
        }
        return super.createTypeId(declaringBean);
    }

    public Collection<Class<? extends Annotation>> getRequiredAnnotations() {
        return requiredTypeAnnotations;
    }

    public Collection<Class<? extends Annotation>> getRequiredScopeAnnotations() {
        if (requiredScopeTypeAnnotations == null) {
            // this init may be performed more than once - which is OK
            ImmutableSet.Builder<Class<? extends Annotation>> builder = ImmutableSet.builder();
            for (Class<? extends Annotation> annotation : requiredTypeAnnotations) {
                if (beanManager.isScope(annotation)) {
                    builder.add(annotation);
                }
            }
            this.requiredScopeTypeAnnotations = builder.build();
        }
        return requiredScopeTypeAnnotations;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExtensionObserverMethodImpl<?, ?> that = (ExtensionObserverMethodImpl<?, ?>) obj;
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
