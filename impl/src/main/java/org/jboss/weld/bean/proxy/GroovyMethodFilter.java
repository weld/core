/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.Method;
import java.util.Set;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.resources.WeldClassLoaderResourceLoader;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * {@link ProxiedMethodFilter} implementation for Groovy. Methods declared by groovy.lang.GroovyObject
 * and their implementations are ignored.
 *
 * @see WELD-840
 * @author Jozef Hartinger
 *
 */
public class GroovyMethodFilter implements ProxiedMethodFilter {

    private static final String GROOVY_OBJECT = "groovy.lang.GroovyObject";

    private static final Set<MethodSignature> METHODS;

    static {
        METHODS = ImmutableSet.<MethodSignature> of(
                new MethodSignatureImpl("invokeMethod", String.class.getName(), Object.class.getName()),
                new MethodSignatureImpl("getProperty", String.class.getName()),
                new MethodSignatureImpl("setProperty", String.class.getName(), Object.class.getName()),
                new MethodSignatureImpl("getMetaClass"),
                new MethodSignatureImpl("setMetaClass", "groovy.lang.MetaClass"),
                new MethodSignatureImpl("$getStaticMetaClass"));
    }

    @Override
    public boolean isEnabled() {
        return Reflections.isClassLoadable(GROOVY_OBJECT, WeldClassLoaderResourceLoader.INSTANCE);
    }

    @Override
    public boolean accept(Method method, Class<?> proxySuperclass) {
        if (GROOVY_OBJECT.equals(method.getDeclaringClass().getName())) {
            return false;
        }
        if (isGroovyObject(proxySuperclass)) {
            for (MethodSignature groovyMethod : METHODS) {
                if (groovyMethod.matches(method)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isGroovyObject(Class<?> clazz) {
        while (clazz != null) {
            for (Class<?> intf : clazz.getInterfaces()) {
                if (GROOVY_OBJECT.equals(intf.getName())) {
                    return true;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }
}
