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
package org.jboss.weld.bean;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.manager.BeanManagerImpl;

import java.util.Collection;

/**
 * Marker for {@link Bean} implementations that are defined by a Java class.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the type of the bean instance
 */
public interface ClassBean<T> extends WeldBean<T> {

    /**
     * Returns the annotated type that defines this bean
     *
     * @return annotated type
     */
    SlimAnnotatedType<T> getAnnotated();

    /**
     * Returns enhanced annotated type metadata. Throws {@link IllegalStateException} if called after bootstrap.
     *
     * @throws IllegalStateException if called after bootstrap
     * @return enhanced annotated type metadata
     */
    EnhancedAnnotatedType<T> getEnhancedAnnotated();

    /**
     * Returns the {@link BeanManager} used by this bean.
     *
     * @return bean manager used by this bean
     */
    BeanManagerImpl getBeanManager();

    /**
     * Returns injection target used to produce instances of this bean
     *
     * @return the injection target
     */
    InjectionTarget<T> getProducer();

    /**
     * Returns a subset of methods of this class bean for which an invoker might be registered.
     * @return a collection of annotated methods for which an invoker can be registered
     */
    Collection<AnnotatedMethod<? super T>> getInvokableMethods();
}
