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
package org.jboss.weld.bean.builtin;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Common implementation for binding-type-based helpers
 *
 * @param <T>
 * @author Gavin King
 */
public abstract class AbstractFacade<T, X> {

    protected static Type getFacadeType(InjectionPoint injectionPoint) {
        Type genericType = injectionPoint.getType();
        if (genericType instanceof ParameterizedType) {
            return ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else {
            throw new IllegalStateException(BeanLogger.LOG.typeParameterMustBeConcrete(injectionPoint));
        }
    }

    private final BeanManagerImpl beanManager;
    private final InjectionPoint injectionPoint;
    // The CreationalContext used to create the facade which was injected.
    // This allows us to propagate the CreationalContext when get() is called
    private final CreationalContext<? super T> creationalContext;

    protected AbstractFacade(InjectionPoint injectionPoint, CreationalContext<? super T> creationalContext,
            BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
        this.injectionPoint = injectionPoint;
        this.creationalContext = creationalContext;
    }

    protected BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    protected Set<Annotation> getQualifiers() {
        return injectionPoint.getQualifiers();
    }

    protected Type getType() {
        return getFacadeType(injectionPoint);
    }

    protected InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    protected CreationalContext<? super T> getCreationalContext() {
        return creationalContext;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractFacade<?, ?>) {
            AbstractFacade<?, ?> that = (AbstractFacade<?, ?>) obj;
            return this.getType().equals(that.getType()) && this.getQualifiers().equals(that.getQualifiers());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += getType().hashCode() * 5;
        hashCode += getQualifiers().hashCode() * 7;
        return hashCode;
    }

    // Serialization

    protected static class AbstractFacadeSerializationProxy<T, X> implements Serializable {

        private static final long serialVersionUID = -9118965837530101152L;

        private final InjectionPoint injectionPoint;
        private final CreationalContext<? super T> creationalContext;
        private final BeanManagerImpl beanManager;

        protected AbstractFacadeSerializationProxy(AbstractFacade<T, X> facade) {
            this.injectionPoint = facade.getInjectionPoint();
            this.beanManager = facade.getBeanManager();
            this.creationalContext = facade.getCreationalContext();
        }

        protected BeanManagerImpl getBeanManager() {
            return beanManager;
        }

        protected InjectionPoint getInjectionPoint() {
            return injectionPoint;
        }

        protected CreationalContext<? super T> getCreationalContext() {
            return creationalContext;
        }

    }

}
