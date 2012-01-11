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
package org.jboss.weld.resolution;

import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 * @author Jozef Hartinger
 */
public class TypeSafeObserverResolver extends TypeSafeResolver<Resolvable, ObserverMethod<?>> {

    private final BeanManagerImpl manager;

    public TypeSafeObserverResolver(BeanManagerImpl manager, Iterable<ObserverMethod<?>> observers) {
        super(observers);
        this.manager = manager;
    }

    @Override
    protected boolean matches(Resolvable resolvable, ObserverMethod<?> observer) {
        return Reflections.matches(observer.getObservedType(), resolvable.getTypes())
                && Beans.containsAllQualifiers(observer.getObservedQualifiers(), resolvable.getQualifiers(), manager)
                // container lifecycle events are fired into Extensions only
                && (!isContainerLifecycleEvent(resolvable) || Extension.class.isAssignableFrom(observer.getBeanClass()));
    }

    protected boolean isContainerLifecycleEvent(Resolvable resolvable) {
        for (Type type : resolvable.getTypes()) {
            if (Observers.CONTAINER_LIFECYCLE_EVENT_TYPES.contains(Reflections.getRawType(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the manager
     */
    public BeanManagerImpl getManager() {
        return manager;
    }

    @Override
    protected Set<ObserverMethod<?>> filterResult(Set<ObserverMethod<?>> matched) {
        return matched;
    }

    @Override
    protected Set<ObserverMethod<?>> sortResult(Set<ObserverMethod<?>> matched) {
        return matched;
    }

}
