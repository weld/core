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
package org.jboss.weld.environment.osgi.impl.integration.discovery.bundle;

import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.collections.EnumerationList;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * A simple resource loader.
 * <p/>
 * Uses {@link BundleResourceLoader}'s classloader if the Thread Context
 * Classloader isn't available
 *
 * @author Pete Muir
 */
public class BundleResourceLoader implements ResourceLoader {
    private final Bundle bundle;

    public BundleResourceLoader(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Class<?> classForName(String name) {
        try {
            //Class<?> clazz = getClassLoader().loadClass(name);
            //Class<?> clazz = getClass().getClassLoader().loadClass(name);
            Class<?> clazz = bundle.loadClass(name);
            // if the class relies on optional dependencies that are not present
            // then a CNFE can be thrown later in the deployment process when the
            // Introspector is inspecting the class. We call getMethods, getFields
            // and getConstructors now over the whole type heirachey to force
            // these errors to occur early.
            // NOTE it is still possible for a CNFE to be thrown at runtime if
            // a class has methods that refer to classes that are not present in
            // their bytecode, this only checks for classes that form part of the
            // class schema that are not present
            Class<?> obj = clazz;
            while (obj != null && obj != Object.class) {
                obj.getDeclaredConstructors();
                obj.getDeclaredFields();
                obj.getDeclaredMethods();
                obj = obj.getSuperclass();
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new ResourceLoadingException(e);
        } catch (NoClassDefFoundError e) {
            throw new ResourceLoadingException(e);
        }
    }

    @Override
    public URL getResource(String name) {
        return bundle.getResource(name);
    }

    @Override
    public Collection<URL> getResources(String name) {
        try {
            return new EnumerationList<URL>(bundle.getResources(name));
        } catch (IOException e) {
            throw new ResourceLoadingException(e);
        }
    }

    @Override
    public void cleanup() {
    }

}
