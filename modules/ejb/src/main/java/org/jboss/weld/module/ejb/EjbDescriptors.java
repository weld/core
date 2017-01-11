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
package org.jboss.weld.module.ejb;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * EJB descriptors by EJB implementation class or name
 *
 * @author Pete Muir
 */
class EjbDescriptors {
    // EJB name -> EJB descriptors map
    private final Map<String, InternalEjbDescriptor<?>> ejbByName;

    private final SetMultimap<Class<?>, String> ejbByClass;

    /**
     * Constructor
     */
    EjbDescriptors(Collection<EjbDescriptor<?>> descriptors) {
        this.ejbByName = new HashMap<String, InternalEjbDescriptor<?>>();
        this.ejbByClass = SetMultimap.newSetMultimap();
        addAll(descriptors);
    }

    /**
     * Gets an iterator to the EJB descriptors for an EJB implementation class
     *
     * @param beanClass The EJB class
     * @return An iterator
     */
    public <T> InternalEjbDescriptor<T> get(String beanName) {
        return cast(ejbByName.get(beanName));
    }

    /**
     * Adds an EJB descriptor to the maps
     *
     * @param ejbDescriptor The EJB descriptor to add
     */
    private <T> void add(EjbDescriptor<T> ejbDescriptor) {
        InternalEjbDescriptor<T> internalEjbDescriptor = InternalEjbDescriptor.of(ejbDescriptor);
        ejbByName.put(ejbDescriptor.getEjbName(), internalEjbDescriptor);
        ejbByClass.put(ejbDescriptor.getBeanClass(), internalEjbDescriptor.getEjbName());
    }

    /**
     * Indicates if there are EJB descriptors available for an EJB implementation
     * class
     *
     * @param beanClass The class to match
     * @return True if present, otherwise false
     */
    public boolean contains(String beanName) {
        return ejbByName.containsKey(beanName);
    }

    /**
     * Indicates if there are EJB descriptors available for an EJB implementation
     * class
     *
     * @param beanClass The class to match
     * @return True if present, otherwise false
     */
    public boolean contains(Class<?> beanClass) {
        return ejbByClass.containsKey(beanClass);
    }

    public <T> InternalEjbDescriptor<T> getUnique(Class<T> beanClass) {
        Set<String> ejbs = ejbByClass.get(beanClass);
        if (ejbs.size() > 1) {
            throw BeanLogger.LOG.tooManyEjbsForClass(beanClass, ejbs);
        } else if (ejbs.size() == 0) {
            return null;
        } else {
            return get(ejbs.iterator().next());
        }
    }

    /**
     * Adds all EJB descriptors to the maps
     *
     * @param ejbDescriptors The descriptors to add
     */
    private void addAll(Iterable<EjbDescriptor<?>> ejbDescriptors) {
        for (EjbDescriptor<?> ejbDescriptor : ejbDescriptors) {
            add(ejbDescriptor);
        }
    }

    public Iterator<InternalEjbDescriptor<?>> iterator() {
        return ejbByName.values().iterator();
    }

    public Collection<InternalEjbDescriptor<?>> getAll() {
        return ejbByName.values();
    }

    public void cleanup() {
        this.ejbByClass.clear();
        this.ejbByName.clear();
    }

}
