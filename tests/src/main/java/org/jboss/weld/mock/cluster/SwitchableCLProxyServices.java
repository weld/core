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

package org.jboss.weld.mock.cluster;

import org.jboss.weld.bean.proxy.util.SimpleProxyServices;
import org.jboss.weld.exceptions.WeldException;

/**
 * Uses a special CL to hold the proxies and allows a test to switch to a new
 * CL. This is useful for testing cluster environments where the VMs are
 * different and thus the CL would also be different between serialization and
 * deserialization.
 *
 * @author David Allen
 */
public class SwitchableCLProxyServices extends SimpleProxyServices {
    private ClassLoader currentClassLoader;

    @Override
    public ClassLoader getClassLoader(Class<?> type) {
        if (currentClassLoader == null) {
            ClassLoader baseClassLoader = super.getClassLoader(type);
            useNewClassLoader(baseClassLoader);
        }
        return currentClassLoader;
    }

    @Override
    public Class<?> loadBeanClass(String className) {
        try {
            return currentClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new WeldException(e);
        }
    }

    public void useNewClassLoader(ClassLoader parentClassLoader) {
        currentClassLoader = new ClusterClassLoader(parentClassLoader);
    }
}
