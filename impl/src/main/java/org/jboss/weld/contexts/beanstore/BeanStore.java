/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.contexts.beanstore;

import java.util.Iterator;
import java.util.Map;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.serialization.spi.BeanIdentifier;

/**
 * A {@link Map} like store of contextual instances, used to back the Weld built
 * in contexts.
 *
 * @author Nicklas Karlsson
 */
public interface BeanStore extends Iterable<BeanIdentifier> {
    /**
     * Gets an instance of a contextual from the store
     *
     * @param id The id of the contextual to return
     * @return The instance or null if not found
     */
    <T> ContextualInstance<T> get(BeanIdentifier id);

    /**
     * Check if the store contains an instance
     *
     * @param id the id of the instance to check for
     * @return true if the instance is present, otherwise false
     */
    boolean contains(BeanIdentifier id);

    /**
     * Clears the store of contextual instances
     */
    void clear();

    Iterator<BeanIdentifier> iterator();

    /**
     * Adds a bean instance to the storage
     *
     * @param contextualInstance the contextual instance
     * @return the id for the instance
     */
    <T> void put(BeanIdentifier id, ContextualInstance<T> contextualInstance);

    /**
     * Gets a creation lock for the given bean id.
     *
     * @param id The bean id
     * @return A handle that must be used to unlock the bean
     */
    LockedBean lock(BeanIdentifier id);

    /**
     * Removes a bean instance identified by the given id.
     *
     * @param id The bean id
     * @return the removed bean instance of null if there was no bean instance before
     */
    <T> ContextualInstance<T> remove(BeanIdentifier id);
}
