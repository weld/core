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
package org.jboss.weld.contexts;

import java.util.List;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.injection.spi.ResourceReference;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @param <T>
 * @author pmuir
 */
@SuppressFBWarnings("NM_SAME_SIMPLE_NAME_AS_INTERFACE")
public interface WeldCreationalContext<T> extends org.jboss.weld.construction.api.WeldCreationalContext<T> {

    <S> WeldCreationalContext<S> getCreationalContext(Contextual<S> contextual);

    /**
     * The returned {@link CreationalContext} shares nothing but incomplete instances.
     *
     * @param contextual
     * @return the {@link CreationalContext} for a producer reciever
     * @see WELD-1513
     */
    <S> WeldCreationalContext<S> getProducerReceiverCreationalContext(Contextual<S> contextual);

    <S> S getIncompleteInstance(Contextual<S> bean);

    void addDependentInstance(ContextualInstance<?> contextualInstance);

    void release();

    /**
     * @return the parent {@link CreationalContext} or null if there isn't any parent.
     */
    WeldCreationalContext<?> getParentCreationalContext();

    /**
     * @return the {@link Contextual} for which this {@link CreationalContext} is created.
     */
    Contextual<T> getContextual();

    /**
     * Returns an unmodifiable list of dependent instances.
     */
    List<ContextualInstance<?>> getDependentInstances();

    /**
     * Destroys dependent instance
     *
     * @param instance
     * @return true if the instance was destroyed, false otherwise
     */
    boolean destroyDependentInstance(T instance);

    /**
     * Register a {@link ResourceReference} as a dependency. {@link ResourceReference#release()} will be called on every
     * {@link ResourceReference}
     * once this {@link CreationalContext} instance is released.
     */
    void addDependentResourceReference(ResourceReference<?> resourceReference);
}
