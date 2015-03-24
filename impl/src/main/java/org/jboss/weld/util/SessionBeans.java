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
package org.jboss.weld.util;

import static org.jboss.weld.util.reflection.Reflections.cast;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

/**
 * Helper class for working with session beans.
 *
 * @author Jozef Hartinger
 *
 */
public class SessionBeans {

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

    public static <T> EnhancedAnnotatedType<T> getEjbImplementationClass(InternalEjbDescriptor<T> ejbDescriptor, BeanManagerImpl manager,
            EnhancedAnnotatedType<T> componentType) {
        if (ejbDescriptor.getBeanClass().equals(ejbDescriptor.getImplementationClass())) {
            // no special implementation class is used
            return componentType;
        }
        ClassTransformer transformer = manager.getServices().get(ClassTransformer.class);
        EnhancedAnnotatedType<T> implementationClass = cast(transformer.getEnhancedAnnotatedType(ejbDescriptor.getImplementationClass(), manager.getId()));
        manager.getServices().get(SlimAnnotatedTypeStore.class).put(implementationClass.slim());
        return implementationClass;
    }
}
