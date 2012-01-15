/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import static org.jboss.weld.logging.messages.UtilMessage.EVENT_TYPE_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.UtilMessage.TYPE_PARAMETER_NOT_ALLOWED_IN_EVENT_TYPE;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessModule;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;

import org.jboss.weld.event.ExtensionObserverMethodImpl;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 */
public class Observers {

    public static final Set<Class<?>> CONTAINER_LIFECYCLE_EVENT_TYPES;

    static {
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(BeforeBeanDiscovery.class);
        types.add(AfterBeanDiscovery.class);
        types.add(AfterDeploymentValidation.class);
        types.add(BeforeShutdown.class);
        types.add(ProcessModule.class);
        types.add(ProcessAnnotatedType.class);
        types.add(ProcessSyntheticAnnotatedType.class);
        types.add(ProcessInjectionPoint.class);
        types.add(ProcessInjectionTarget.class);
        types.add(ProcessProducer.class);
        // TODO ProcessProducerMethod and ProcessProducerField from 11.5.9 missing?
        types.add(ProcessBeanAttributes.class);
        types.add(ProcessBean.class);
        types.add(ProcessSessionBean.class);
        types.add(ProcessManagedBean.class);
        types.add(ProcessProducerMethod.class);
        types.add(ProcessProducerField.class);
        types.add(ProcessObserverMethod.class);
        CONTAINER_LIFECYCLE_EVENT_TYPES = Collections.unmodifiableSet(types);
    }

    public static void checkEventObjectType(Type eventType) {
        Type[] types;
        Type resolvedType = new HierarchyDiscovery(eventType).getResolvedType();
        if (resolvedType instanceof Class<?>) {
            types = new Type[0];
        } else if (resolvedType instanceof ParameterizedType) {
            types = ((ParameterizedType) resolvedType).getActualTypeArguments();
        } else {
            throw new IllegalArgumentException(EVENT_TYPE_NOT_ALLOWED, resolvedType);
        }
        for (Type type : types) {
            if (type instanceof TypeVariable<?>) {
                throw new IllegalArgumentException(TYPE_PARAMETER_NOT_ALLOWED_IN_EVENT_TYPE, resolvedType);
            }
        }
    }

    public static void checkEventObjectType(Object event) {
        checkEventObjectType(event.getClass());
    }

    public static boolean isContainerLifecycleObserverMethod(ObserverMethod<?> method) {
        return CONTAINER_LIFECYCLE_EVENT_TYPES.contains(Reflections.getRawType(method.getObservedType()))
                || (Object.class.equals(method.getObservedType()) && method instanceof ExtensionObserverMethodImpl<?, ?>);
    }

    public static boolean isObserverMethodEnabled(ObserverMethod<?> method, BeanManagerImpl manager) {
        if (method instanceof ObserverMethodImpl<?, ?>) {
            Bean<?> declaringBean = Reflections.<ObserverMethodImpl<?, ?>> cast(method).getDeclaringBean();
            return manager.isBeanEnabled(declaringBean) && !Beans.isSpecialized(declaringBean, manager) && !Beans.isSuppressedBySpecialization(declaringBean, manager);
        }
        return true;
    }
}
