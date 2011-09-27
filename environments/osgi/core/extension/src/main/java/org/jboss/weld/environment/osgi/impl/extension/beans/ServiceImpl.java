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
package org.jboss.weld.environment.osgi.impl.extension.beans;

import org.jboss.weld.environment.osgi.impl.extension.FilterGenerator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.Filter;

/**
 * Implementation of {@link Service}.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class ServiceImpl<T> implements Service<T> {

    private static Logger logger = LoggerFactory.getLogger(ServiceImpl.class);

    private final Class serviceClass;

    private final BundleContext registry;

    private final String serviceName;

    private List<T> services = new ArrayList<T>();

    private T service = null;

    private Filter filter;

    public ServiceImpl(Type type, BundleContext registry) {
        logger.trace("Entering ServiceImpl : "
                     + "ServiceImpl() with parameters {} | {}",
                     new Object[] {type, registry});
        serviceClass = (Class) type;
        serviceName = serviceClass.getName();
        this.registry = registry;
        filter = FilterGenerator.makeFilter();
        logger.debug("New ServiceImpl constructed {}", this);
    }

    public ServiceImpl(Type type, BundleContext registry, Filter filter) {
        logger.trace("Entering ServiceImpl : "
                     + "ServiceImpl() with parameters {} | {} | {}",
                     new Object[] {type, registry, filter});
        serviceClass = (Class) type;
        serviceName = serviceClass.getName();
        this.registry = registry;
        this.filter = filter;
        logger.debug("New ServiceImpl constructed {}", this);
    }

    @Override
    public T get() {
        populateServices();
        return service;
    }

    private void populateServices() {
        logger.trace("Entering ServiceImpl : "
                     + "populateServices() with no parameter");
        services.clear();
        String filterString = null;
        if (filter != null && !filter.value().equals("")) {
            filterString = filter.value();
        }
        /* ServiceTracker usage, currently fails
        ServiceTracker tracker = new ServiceTracker(registry, registry.createFilter(
        "(&(objectClass=" + serviceName + ")" + filterString + ")"), null);
        tracker.open();
        Object[] instances = tracker.getServices();
        if (instances != null) {
        for (Object ref : instances) {
        services.add((T) ref);
        }
        }
        service = services.size() > 0 ? services.get(0) : null;*/
        ServiceReference[] refs = null;
        try {
            refs = registry.getServiceReferences(serviceName, filterString);
        }
        catch(InvalidSyntaxException ex) {
            logger.warn("Unblale to find service references "
                        + "for service {} with filter {} due to {}",
                        new Object[] {
                        serviceName,
                        filterString,
                        ex
                    });
        }
        if (refs != null) {
            for (ServiceReference ref : refs) {
                if (!serviceClass.isInterface()) {
                    services.add((T) registry.getService(ref));
                }
                else {
                    services.add((T) Proxy.newProxyInstance(
                            getClass().getClassLoader(),
                            new Class[] {
                                (Class) serviceClass
                            },
                            new ServiceReferenceHandler(ref, registry)));
                }
            }
        }
        service = services.size() > 0 ? services.get(0) : null;
    }

    @Override
    public Service<T> select(Annotation... qualifiers) {
        service = null;
        filter = FilterGenerator.makeFilter(filter, Arrays.asList(qualifiers));
        return this;
    }

    @Override
    public Service<T> select(String filter) {
        service = null;
        this.filter = FilterGenerator.makeFilter(this.filter, filter);
        return this;
    }

    @Override
    public boolean isUnsatisfied() {
        return (size() <= 0);
    }

    @Override
    public boolean isAmbiguous() {
        return (size() > 1);
    }

    @Override
    public int size() {
        if (service == null) {
            try {
                populateServices();
            }
            catch(Exception e) {
                e.printStackTrace();
                return -1;
            }
        }
        return services.size();
    }

    @Override
    public Iterator<T> iterator() {
        try {
            populateServices();
        }
        catch(Exception ex) {
            ex.printStackTrace();
            services = Collections.emptyList();
        }
        return services.iterator();
    }

    @Override
    public String toString() {
        return "ServiceImpl{ Service class "
               + serviceName + " with filter "
               + filter.value() + '}';
    }

    @Override
    public Iterable<T> first() {
        return new Iterable<T>() {

            @Override
            public Iterator<T> iterator() {
                try {
                    populateServices();
                }
                catch(Exception ex) {
                    return Collections.<T>emptyList().iterator();
                }
                if (services.isEmpty()) {
                    return Collections.<T>emptyList().iterator();
                }
                else {
                    return Collections.singletonList(services.get(0)).iterator();
                }
            }

        };
    }

}