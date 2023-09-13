/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import java.lang.reflect.Constructor;

import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Implementation of this interface is capable of producing Java objects. This abstraction allows different strategies to be
 * employed
 * in a component creation process, e.g. {@link DefaultInstantiator} or {@link SubclassedComponentInstantiator}.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public interface Instantiator<T> {

    T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager);

    /**
     * Indicates whether instances created by this Instantiator support interception.
     */
    boolean hasInterceptorSupport();

    /**
     * Indicates whether instances created by this Instantiator support decorators.
     */
    boolean hasDecoratorSupport();

    /**
     * Returns the constructor used for instantiation. If an enhanced subclass is used for a component instance, this method
     * returns the matching constructor of the original component class.
     */
    Constructor<T> getConstructor();
}
