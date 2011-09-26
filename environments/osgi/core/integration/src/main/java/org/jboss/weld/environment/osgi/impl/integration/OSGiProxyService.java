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

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.serialization.spi.ProxyServices;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 */
public class OSGiProxyService implements ProxyServices {
    private final ClassLoader loader;

    public OSGiProxyService() {
        this.loader = getClass().getClassLoader();
    }

    @Override
    public ClassLoader getClassLoader(Class<?> proxiedBeanType) {
        return new BridgeClassLoader(proxiedBeanType.getClassLoader(), loader);
    }

    @Override
    public Class<?> loadBeanClass(final String className) {
        try {
            return (Class<?>) AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Object>() {
                        public Object run() throws Exception {
                            return Class.forName(className,
                                    true,
                                    getClassLoader(this.getClass()));
                        }

                    });
        } catch (PrivilegedActionException pae) {
            throw new WeldException(BeanMessage.CANNOT_LOAD_CLASS,
                    className,
                    pae.getException());
        }
    }

    @Override
    public void cleanup() {
        // no cleanup
    }

    private static class BridgeClassLoader extends ClassLoader {
        private final ClassLoader delegate;

        private final ClassLoader infra;

        public BridgeClassLoader(ClassLoader delegate, ClassLoader infraClassLoader) {
            this.delegate = delegate;
            this.infra = infraClassLoader;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> loadedClass = null;
            try {
                loadedClass = delegate.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // todo : filter on utils class only
                loadedClass = infra.loadClass(name);
            }
            return loadedClass;
        }

    }
}
