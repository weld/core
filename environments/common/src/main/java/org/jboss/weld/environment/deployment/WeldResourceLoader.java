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
package org.jboss.weld.environment.deployment;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.jboss.weld.environment.util.Collections;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

/**
 * A simple resource loader.
 * <p/>
 * Uses {@link WeldResourceLoader}'s classloader if the Thread Context
 * Classloader isn't available
 *
 * @author Pete Muir
 */
public class WeldResourceLoader implements ResourceLoader {

    public Class<?> classForName(String name) {

        try {
            return getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new ResourceLoadingException(e);
        } catch (LinkageError e) {
            throw new ResourceLoadingException(e);
        }
    }

    public URL getResource(String name) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null) {
            return tccl.getResource(name);
        } else {
            return getClass().getResource(name);
        }
    }

    public Collection<URL> getResources(String name) {
        try {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            if (tccl != null) {
                return Collections.asList(tccl.getResources(name));
            } else {
                return Collections.asList(getClass().getClassLoader().getResources(name));
            }
        } catch (IOException e) {
            throw new ResourceLoadingException(e);
        }
    }

    public void cleanup() {
    }

    public static ClassLoader getClassLoader() {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null) {
            return tccl;
        } else {
            return WeldResourceLoader.class.getClassLoader();
        }
    }

}
