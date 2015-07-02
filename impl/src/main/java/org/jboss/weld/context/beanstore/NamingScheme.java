/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.context.beanstore;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.weld.serialization.spi.BeanIdentifier;

public interface NamingScheme {

    /**
     * Determine if this identifier has been prefixed
     *
     * @param id the id to check
     * @return true if it has been prefixed, false otherwise
     */
    boolean accept(String id);

    /**
     * Remove the prefix from the id
     *
     * @param id the prefixed id
     * @return the id without the prefix
     */
    BeanIdentifier deprefix(String id);

    /**
     * Add the prefix to the id
     *
     * @param id the id to prefix
     * @return the prefixed id
     */
    String prefix(BeanIdentifier id);

    /**
     * Filter ids and retain only those correctly prefixed.
     *
     * @param ids the identifiers to filter
     */
    Collection<String> filterIds(Iterator<String> ids);

    Collection<BeanIdentifier> deprefix(Collection<String> ids);

    Collection<String> prefix(Collection<BeanIdentifier> ids);

}
