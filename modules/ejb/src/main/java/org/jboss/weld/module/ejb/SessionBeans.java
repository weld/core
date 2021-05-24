/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module.ejb;

import static org.jboss.weld.serialization.spi.BeanIdentifier.BEAN_ID_SEPARATOR;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.attributes.BeanAttributesFactory.BeanAttributesBuilder;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Helper class for working with session beans.
 *
 * @author Jozef Hartinger
 *
 */
class SessionBeans {

    private SessionBeans() {
    }

    /**
     * Returns {@link EnhancedAnnotatedType} for the EJB implementation class. Throws {@link IllegalStateException} if called after bootstrap.
     *
     * @param bean
     * @throws IllegalStateException if called after bootstrap
     * @return {@link EnhancedAnnotatedType} representation of this EJB's implementation class
     */
    public static <T> EnhancedAnnotatedType<T> getEjbImplementationClass(SessionBean<T> bean) {
        return getEjbImplementationClass(bean.getEjbDescriptor(), bean.getBeanManager(), bean.getEnhancedAnnotated());
    }

    public static <T> EnhancedAnnotatedType<T> getEjbImplementationClass(EjbDescriptor<T> descriptor, BeanManagerImpl manager,
            EnhancedAnnotatedType<T> componentType) {
        InternalEjbDescriptor<T> ejbDescriptor = InternalEjbDescriptor.of(descriptor);
        if (ejbDescriptor.getBeanClass().equals(ejbDescriptor.getImplementationClass())) {
            // no special implementation class is used
            return componentType;
        }
        ClassTransformer transformer = manager.getServices().get(ClassTransformer.class);
        EnhancedAnnotatedType<T> implementationClass = cast(transformer.getEnhancedAnnotatedType(ejbDescriptor.getImplementationClass(), manager.getId()));
        manager.getServices().get(SlimAnnotatedTypeStore.class).put(implementationClass.slim());
        return implementationClass;
    }

    /*
     * Identifiers
     */

    public static String createIdentifier(EnhancedAnnotatedType<?> type, EjbDescriptor<?> descriptor) {
        StringBuilder builder = BeanIdentifiers.getPrefix(SessionBean.class);
        appendEjbNameAndClass(builder, descriptor);
        if (!type.isDiscovered()) {
            builder.append(BEAN_ID_SEPARATOR).append(type.slim().getIdentifier().asString());
        }
        return builder.toString();
    }

    private static StringBuilder appendEjbNameAndClass(StringBuilder builder, EjbDescriptor<?> descriptor) {
        return builder.append(descriptor.getEjbName()).append(BEAN_ID_SEPARATOR).append(descriptor.getBeanClass().getName());
    }

    /*
     * Bean attributes
     */
    /**
     * Creates new {@link BeanAttributes} to represent a session bean.
     */
    public static <T> BeanAttributes<T> createBeanAttributes(EnhancedAnnotatedType<T> annotated, InternalEjbDescriptor<?> descriptor, BeanManagerImpl manager) {
        final Set<Type> types = SharedObjectCache.instance(manager).getSharedSet(getSessionBeanTypes(annotated, Reflections.<InternalEjbDescriptor<T>> cast(descriptor)));
        return new BeanAttributesBuilder<T>(annotated, types, manager).build();
    }

    /**
     * Bean types of a session bean.
     */
    private static <T> Set<Type> getSessionBeanTypes(EnhancedAnnotated<T, ?> annotated, EjbDescriptor<T> ejbDescriptor) {
        ImmutableSet.Builder<Type> types = ImmutableSet.builder();
        // session beans
        Map<Class<?>, Type> typeMap = new LinkedHashMap<Class<?>, Type>();
        HierarchyDiscovery beanClassDiscovery = HierarchyDiscovery.forNormalizedType(ejbDescriptor.getBeanClass());
        for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces()) {
            // first we need to resolve the local interface
            Type resolvedLocalInterface = beanClassDiscovery.resolveType(Types.getCanonicalType(businessInterfaceDescriptor.getInterface()));
            SessionBeanHierarchyDiscovery interfaceDiscovery = new SessionBeanHierarchyDiscovery(resolvedLocalInterface);
            if (beanClassDiscovery.getTypeMap().containsKey(businessInterfaceDescriptor.getInterface())) {
                // WELD-1675 Only add types also included in Annotated.getTypeClosure()
                for (Entry<Class<?>, Type> entry : interfaceDiscovery.getTypeMap().entrySet()) {
                    if (annotated.getTypeClosure().contains(entry.getValue())) {
                        typeMap.put(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                // Session bean class does not implement the business interface and @jakarta.ejb.Local applied to the session bean class
                typeMap.putAll(interfaceDiscovery.getTypeMap());
            }
        }
        if (annotated.isAnnotationPresent(Typed.class)) {
            types.addAll(Beans.getTypedTypes(typeMap, annotated.getJavaClass(), annotated.getAnnotation(Typed.class)));
        } else {
            typeMap.put(Object.class, Object.class);
            types.addAll(typeMap.values());
        }
        return Beans.getLegalBeanTypes(types.build(), annotated);
    }
}
