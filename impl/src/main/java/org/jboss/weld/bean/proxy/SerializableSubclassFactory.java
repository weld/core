/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.classfilewriter.ClassFile;
import org.jboss.weld.security.GetDeclaredConstructorsAction;
import org.jboss.weld.util.bytecode.ConstructorUtils;
import org.jboss.weld.util.bytecode.DeferredBytecode;
import org.jboss.weld.util.bytecode.DescriptorUtils;

public class SerializableSubclassFactory<T> extends ProxyFactory<T> {

    public static final String PROXY_SUFFIX = "SerializableSubclass";

    public SerializableSubclassFactory(Class<?> proxiedBeanType, Bean<?> bean) {
        super(proxiedBeanType, Collections.<Class<?>> emptySet(), bean);
    }

    @Override
    protected void addFields(ClassFile proxyClassType, List<DeferredBytecode> initialValueBytecode, boolean useConstructedFlag) {
        // noop
    }

    @Override
    protected void addMethods(ClassFile proxyClassType) {
        // noop
    }

    @Override
    protected void addConstructors(ClassFile proxyClassType, List<DeferredBytecode> initialValueBytecode, boolean useConstructedFlag) {
        for (Constructor<?> constructor : AccessController.doPrivileged(new GetDeclaredConstructorsAction(getBeanType()))) {
            if ((constructor.getModifiers() & Modifier.PRIVATE) == 0) {
                String[] exceptions = new String[constructor.getExceptionTypes().length];
                for (int i = 0; i < exceptions.length; ++i) {
                    exceptions[i] = constructor.getExceptionTypes()[i].getName();
                }
                ConstructorUtils.addConstructor("V", DescriptorUtils.getParameterTypes(constructor.getParameterTypes()), exceptions, proxyClassType, initialValueBytecode, false);
            }
        }
    }

    @Override
    protected String getProxyNameSuffix() {
        return PROXY_SUFFIX;
    }

    @Override
    protected Set<Class<?>> getSpecialInterfaces() {
        return Collections.emptySet();
    }
}
