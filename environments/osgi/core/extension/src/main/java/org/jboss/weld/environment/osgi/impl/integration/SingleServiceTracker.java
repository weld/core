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
package org.jboss.weld.environment.osgi.impl.integration;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A helper object to track a single instance of an OSGi service
 * @param <T>
 *
 * @author Guillaume NODET (gnodet@gmail.com)
 */
public class SingleServiceTracker<T> {

    public interface Listener<T> {
        void bind(T service);
    }

    private final BundleContext context;
    private final Class<T> clazz;
    private final Listener<T> listener;
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicReference<T> service = new AtomicReference<T>();
    private final AtomicReference<ServiceReference> reference = new AtomicReference<ServiceReference>();
    private final ServiceListener serviceListener = new ServiceListener() {
        @Override
        public void serviceChanged(ServiceEvent event) {
            onServiceChanged(event);
        }
    };

    public SingleServiceTracker(BundleContext context, Class<T> clazz, Listener<T> listener) {
        this.context = context;
        this.clazz = clazz;
        this.listener = listener;
    }

    public void open() {
        if (started.compareAndSet(false, true)) {
            String filter = '(' + Constants.OBJECTCLASS + '=' + clazz.getName() + ')';
            try {
                context.addServiceListener(serviceListener, filter);
            } catch (InvalidSyntaxException e) {
                throw new RuntimeException(e);
            }
            findMatchingReference();
        }
    }

    public void close() {
        if (started.compareAndSet(true, false)) {
            context.removeServiceListener(serviceListener);
            update(null);
        }
    }

    public ServiceReference getReference() {
        return reference.get();
    }

    public T getService() {
        return service.get();
    }

    private void onServiceChanged(ServiceEvent event) {
        if (started.get()) {
            if (event.getType() == ServiceEvent.UNREGISTERING) {
                ServiceReference deadRef = event.getServiceReference();
                if (deadRef.equals(reference.get())) {
                    findMatchingReference();
                }
            } else if (event.getType() == ServiceEvent.REGISTERED && reference.get() == null) {
                findMatchingReference();
            }
        }
    }

    private void findMatchingReference() {
        ServiceReference ref = context.getServiceReference(clazz.getName());
        update(ref);
    }

    private void update(ServiceReference newReference) {
        T newService = newReference != null ? clazz.cast(context.getService(newReference)) : null;
        if (newService == null) {
            newReference = null;
        }
        ServiceReference oldReference;
        synchronized (this) {
            oldReference = reference.getAndSet(newReference);
            service.set(newService);
        }
        if (oldReference != null) {
            context.ungetService(oldReference);
        }
        if (listener != null) {
            listener.bind(newService);
        }
    }

}
