/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.serialization.spi.ProxyServices;

/**
 * A default implementation of the {@link ProxyServices} which simply use the
 * corresponding information from the proxy type. An exception is made for
 * {@code java.*} and {@code javax.*} packages which are often associated with
 * the system classloader and a more privileged ProtectionDomain.
 *
 * @author David Allen
 */
public class SimpleProxyServices implements ProxyServices {

    public ClassLoader getClassLoader(final Class<?> proxiedBeanType) {
        return proxiedBeanType.getClassLoader();
    }

    public void cleanup() {
        // This implementation requires no cleanup

    }

    @Deprecated
    public Class<?> loadBeanClass(final String className) {
        try {
            return (Class<?>) AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() throws Exception {
                    return Class.forName(className, true, getClassLoader(this.getClass()));
                }
            });
        } catch (PrivilegedActionException pae) {
            throw BeanLogger.LOG.cannotLoadClass(className, pae.getException());
        }
    }

}
