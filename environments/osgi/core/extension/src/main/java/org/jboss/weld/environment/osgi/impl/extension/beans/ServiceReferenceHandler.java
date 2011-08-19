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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @deprecated
 * {@link DynamicServiceHandler}
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
public class ServiceReferenceHandler implements InvocationHandler {

    private final ServiceReference ref;
    private final BundleContext registry;

    public ServiceReferenceHandler(ServiceReference ref, BundleContext registry) {
        this.ref = ref;
        this.registry = registry;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object instanceToUse = registry.getService(ref);
        try {
            return method.invoke(instanceToUse, args);
        } finally {
            registry.ungetService(ref);
        }
    }
}
