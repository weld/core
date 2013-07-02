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
package org.jboss.weld.bean;

import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * Represents a @New simple bean
 *
 * @author Nicklas Karlsson
 */
public class NewManagedBean<T> extends ManagedBean<T> implements NewBean {

    /**
     * Creates an instance of a NewSimpleBean from an annotated class
     *
     * @param clazz       The annotated class
     * @param beanManager The Bean manager
     * @return a new NewSimpleBean instance
     */
    public static <T> NewManagedBean<T> of(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> clazz, BeanManagerImpl beanManager) {
        return new NewManagedBean<T>(attributes, clazz, new StringBeanIdentifier(BeanIdentifiers.forNewManagedBean(clazz)), beanManager);
    }

    /**
     * Protected constructor
     *
     * @param type        An annotated class
     * @param beanManager The Bean manager
     */
    protected NewManagedBean(BeanAttributes<T> attributes, final EnhancedAnnotatedType<T> type, BeanIdentifier identifier, BeanManagerImpl beanManager) {
        super(attributes, type, identifier, beanManager);
    }

    @Override
    public boolean isSpecializing() {
        return false;
    }

    @Override
    public String toString() {
        return "@New " + super.toString();
    }
}
