/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.osgi.api.events;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>This abstract class represents all the Weld-OSGi service events as a
 * superclass.</p>
 * <p>It allows to:<ul>
 * <li>
 * <p>Represent all service events,</p>
 * </li>
 * <li>
 * <p>Retrieve the current event type as a
 * {@link ServiceEventType},</p>
 * </li>
 * <li>
 * <p>Retrieve the affected {@link ServiceReference}, the
 * corresponding information and registering
 * {@link Bundle},</p>
 * </li>
 * <li>
 * <p>Manipulate the service,</p>
 * </li>
 * <li>
 * <p>Retrieve the firing {@link BundleContext}.</p>
 * </li>
 * </ul></p>
 * <p>It may be used in {@link javax.enterprise.event.Observes} method in order
 * to listen all service events.</p>
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 * @see Bundle
 * @see BundleContext
 * @see ServiceReference
 * @see ServiceEvents
 * @see ServiceEventType
 * @see javax.enterprise.event.Observes
 */
public abstract class AbstractServiceEvent {
    private final ServiceReference reference;

    private final BundleContext context;

    private List<String> classesNames;

    private List<Class<?>> classes;

    private Map<Class, Boolean> assignable = new HashMap<Class, Boolean>();

    /**
     * Construct a new service event for the specified service.
     *
     * @param reference the {@link ServiceReference} that changes of state.
     * @param context   the service {@link BundleContext}.
     */
    public AbstractServiceEvent(ServiceReference reference, BundleContext context) {
        this.reference = reference;
        this.context = context;
    }

    /**
     * Get the service event type.
     *
     * @return the {@link ServiceEventType} of the fired service event.
     */
    public abstract ServiceEventType eventType();

    /**
     * Get the service reference of the firing service.
     *
     * @return the {@link ServiceReference} of the fired service event.
     */
    public ServiceReference getReference() {
        return reference;
    }

    /**
     * Get a service instance of the firing service.
     *
     * @return the service instance of the firing service.
     * @see BundleContext#getService(org.osgi.framework.ServiceReference)
     */
    public Object getService() {
        return context.getService(reference);
    }

    /**
     * Release the service instance of the firing service.
     *
     * @return false if the service instance is already released or if the
     *         service is unavailable, true otherwhise.
     * @see BundleContext#ungetService(org.osgi.framework.ServiceReference)
     */
    public boolean ungetService() {
        return context.ungetService(reference);
    }

    /**
     * Get the registering bundle of the firing service.
     *
     * @return the registering bundle.
     */
    public Bundle getRegisteringBundle() {
        return reference.getBundle();
    }

    /**
     * Get the class names under which the firing service was registered.
     *
     * @return all the class names for the firing service.
     */
    public List<String> getServiceClassNames() {
        if (classesNames == null) {
            classesNames = Arrays.asList(
                    (String[]) reference.getProperty(Constants.OBJECTCLASS));
        }
        return classesNames;
    }

    /**
     * Get a service instance of the firing service with the specific type.
     *
     * @param type the wanted class for the service instance
     * @param <T>  the wanted type for the service instance
     * @return the service instance of the firing service with the given type.
     * @see BundleContext#getService(org.osgi.framework.ServiceReference)
     */
    public <T> T getService(Class<T> type) {
        if (isTyped(type)) {
            return type.cast(getService());
        } else {
            throw new RuntimeException("the type " + type
                    + " isn't supported for the service."
                    + " Supported types are "
                    + getServiceClasses(type));
        }
    }

    /**
     * If the specified type is a implementation of the firing service.
     *
     * @param type the tested type for being a firing service implementation.
     * @return true if the specified type is assignable for the firing service,
     *         false otherwise.
     */
    public boolean isTyped(Class<?> type) {
        boolean typed = false;
        if (!assignable.containsKey(type)) {
            for (Class clazz : getServiceClasses(type)) {
                if (type.isAssignableFrom(clazz)) {
                    typed = true;
                    break;
                }
            }
            assignable.put(type, typed);
        }
        return assignable.get(type);
    }

    /**
     * Get the class that are the firing service implementations.
     *
     * @param type the class from which the service will be loaded
     * @return all the firing service implementation classes.
     */
    public List<Class<?>> getServiceClasses(Class<?> type) {
        if (classes == null) {
            classes = new ArrayList<Class<?>>();
            for (String className : getServiceClassNames()) {
                try {
                    classes.add(type.getClassLoader().loadClass(className));
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    return Collections.emptyList();
                }
            }
        }
        return classes;
    }

}