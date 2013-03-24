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
package org.jboss.weld.injection;

import static org.jboss.weld.logging.messages.BeanMessage.INVALID_RESOURCE_PRODUCER_TYPE;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JaxwsInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.util.DelegatingFieldInjectionPointAttributes;
import org.jboss.weld.util.reflection.Reflections;

/**
 *
 * @author Jozef Hartinger
 *
 * @param <T> injection point type
 * @param <X> the declaring class
 */
public class ResourceInjectionPoint<T, X> extends DelegatingFieldInjectionPointAttributes<T, X> {

    public static <T, X> ResourceInjectionPoint<T, X> forEjb(FieldInjectionPoint<T, X> delegate, EjbInjectionServices injectionServices) {
        return new ResourceInjectionPoint<T, X>(delegate, Reflections.<ResourceReferenceFactory<T>>cast(injectionServices.registerEjbInjectionPoint(delegate)));
    }

    public static <T, X> ResourceInjectionPoint<T, X> forPersistenceContext(FieldInjectionPoint<T, X> delegate, JpaInjectionServices injectionServices, PersistenceApiAbstraction api) {
        if (!delegate.getType().equals(api.ENTITY_MANAGER_CLASS)) {
            throw new DefinitionException(INVALID_RESOURCE_PRODUCER_TYPE, delegate.getAnnotated(), api.ENTITY_MANAGER_CLASS);
        }
        return new ResourceInjectionPoint<T, X>(delegate, Reflections.<ResourceReferenceFactory<T>>cast(injectionServices.registerPersistenceContextInjectionPoint(delegate)));
    }

    public static <T, X> ResourceInjectionPoint<T, X> forPersistenceUnit(FieldInjectionPoint<T, X> delegate, JpaInjectionServices injectionServices, PersistenceApiAbstraction api) {
        if (!delegate.getType().equals(api.ENTITY_MANAGER_FACTORY_CLASS)) {
            throw new DefinitionException(INVALID_RESOURCE_PRODUCER_TYPE, delegate.getAnnotated(), api.ENTITY_MANAGER_FACTORY_CLASS);
        }
        return new ResourceInjectionPoint<T, X>(delegate, Reflections.<ResourceReferenceFactory<T>>cast(injectionServices.registerPersistenceUnitInjectionPoint(delegate)));
    }

    public static <T, X> ResourceInjectionPoint<T, X> forResource(FieldInjectionPoint<T, X> delegate, ResourceInjectionServices injectionServices) {
        return new ResourceInjectionPoint<T, X>(delegate, Reflections.<ResourceReferenceFactory<T>>cast(injectionServices.registerResourceInjectionPoint(delegate)));
    }

    public static <T, X> ResourceInjectionPoint<T, X> forWebServiceRef(FieldInjectionPoint<T, X> delegate, JaxwsInjectionServices injectionServices) {
        return new ResourceInjectionPoint<T, X>(delegate, Reflections.<ResourceReferenceFactory<T>>cast(injectionServices.registerWebServiceRefInjectionPoint(delegate)));
    }

    private final FieldInjectionPoint<T, X> delegate;
    private final ResourceReferenceFactory<T> factory;

    private ResourceInjectionPoint(FieldInjectionPoint<T, X> delegate, ResourceReferenceFactory<T> factory) {
        this.delegate = delegate;
        this.factory = factory;
    }

    public T getReference(CreationalContext<?> ctx) {
        ResourceReference<T> reference = factory.createResource();
        if (ctx instanceof WeldCreationalContext<?>) {
            Reflections.<WeldCreationalContext<?>>cast(ctx).addDependentResourceReference(reference);
        }
        return reference.getInstance();
    }

    public void inject(Object declaringInstance, CreationalContext<?> ctx) {
        delegate.inject(declaringInstance, getReference(ctx));
    }

    @Override
    protected FieldInjectionPointAttributes<T, X> delegate() {
        return delegate.delegate();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceInjectionPoint<?, ?> other = (ResourceInjectionPoint<?, ?>) obj;
        if (delegate == null) {
            if (other.delegate != null)
                return false;
        } else if (!delegate.equals(other.delegate))
            return false;
        return true;
    }
}