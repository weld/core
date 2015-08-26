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
package org.jboss.weld.injection;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.injection.spi.ResourceReferenceFactory;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Abstract resource injection.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
abstract class AbstractResourceInjection<T> implements ResourceInjection<T> {

    private final ResourceReferenceFactory<T> factory;

    AbstractResourceInjection(ResourceReferenceFactory<T> factory) {
        Preconditions.checkNotNull(factory);
        this.factory = factory;
    }

    @Override
    public T getResourceReference(CreationalContext<?> ctx) {
        ResourceReference<T> reference = factory.createResource();
        if (ctx instanceof WeldCreationalContext<?>) {
            Reflections.<WeldCreationalContext<?>> cast(ctx).addDependentResourceReference(reference);
        }
        return reference.getInstance();
    }

    @Override
    public void injectResourceReference(Object declaringInstance, CreationalContext<?> ctx) {
        injectMember(declaringInstance, getResourceReference(ctx));
    }

    protected abstract void injectMember(Object declaringInstance, Object reference);

}
