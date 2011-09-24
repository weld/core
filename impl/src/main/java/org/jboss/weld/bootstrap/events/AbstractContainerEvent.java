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
package org.jboss.weld.bootstrap.events;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import javax.enterprise.inject.spi.ObserverMethod;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractContainerEvent {

    private final List<Throwable> errors;
    private final BeanManagerImpl beanManager;
    private final Type[] actualTypeArguments;
    private final Type rawType;

    protected AbstractContainerEvent(BeanManagerImpl beanManager, Type rawType, Type[] actualTypeArguments) {
        this.errors = new ArrayList<Throwable>();
        this.beanManager = beanManager;
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
    }

    /**
     * @return the errors
     */
    protected List<Throwable> getErrors() {
        return errors;
    }

    protected BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    protected void fire() {
        Type eventType = new ParameterizedTypeImpl(getRawType(), getActualTypeArguments(), null);
        try {
            beanManager.fireEvent(eventType, this);
        } catch (Exception e) {
            getErrors().add(e);
        }
    }

    protected void fire(Map<BeanDeploymentArchive, BeanDeployment> beanDeployments) {
        try {
            // Collect all observers to remove dupes
            Set<ObserverMethod<Object>> observers = new HashSet<ObserverMethod<Object>>();
            Type eventType = new ParameterizedTypeImpl(getRawType(), getActualTypeArguments(), null);
            for (BeanDeployment beanDeployment : beanDeployments.values()) {
                observers.addAll(beanDeployment.getBeanManager().resolveObserverMethods(eventType));
            }
            for (ObserverMethod<Object> observerMethod : observers) {
                observerMethod.notify(this);
            }
        } catch (Exception e) {
            getErrors().add(e);
        }
    }

    protected Type getRawType() {
        return rawType;
    }

    protected Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

}