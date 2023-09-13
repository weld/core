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
package org.jboss.weld.interceptor.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import jakarta.interceptor.InvocationContext;

/**
 * Simple {@link InvocationContext} implementation whose {@link #proceed()} invokes the target method directly without calling
 * any interceptors. If this is not
 * a method interception, a call to {@link #proceed()} always returns null.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 * @author Jozef Hartinger
 */
public class SimpleInvocationContext extends AbstractInvocationContext {

    public SimpleInvocationContext(Object target, Method targetMethod, Method proceed, Object[] parameters,
            Set<Annotation> interceptorBindings) {
        super(target, targetMethod, proceed, null, parameters, null, null, interceptorBindings);
    }

    public SimpleInvocationContext(Constructor<?> constructor, Object[] parameters, Map<String, Object> contextData,
            Set<Annotation> interceptorBindings) {
        super(null, null, null, constructor, parameters, null, contextData, interceptorBindings);
    }

    @Override
    public Object proceed() throws Exception {
        if (proceed != null) {
            return proceed.invoke(target, parameters);
        } else {
            return null;
        }
    }
}
