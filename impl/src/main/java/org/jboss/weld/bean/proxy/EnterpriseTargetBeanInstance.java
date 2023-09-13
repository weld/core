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

package org.jboss.weld.bean.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author David Allen
 * @author Marko Luksa
 */
public class EnterpriseTargetBeanInstance extends AbstractBeanInstance implements Serializable {
    private static final long serialVersionUID = 2825052095047112162L;

    private final Class<?> beanType;
    private final MethodHandler methodHandler;

    public EnterpriseTargetBeanInstance(Class<?> baseType, MethodHandler methodHandler) {
        this.beanType = baseType;
        this.methodHandler = methodHandler;
    }

    public EnterpriseTargetBeanInstance(Set<Type> types, MethodHandler methodHandler) {
        this(computeInstanceType(types), methodHandler);
    }

    public Object getInstance() {
        return null;
    }

    public Class<?> getInstanceType() {
        return beanType;
    }

    public Object invoke(Object instance, Method method, Object... arguments) throws Throwable {
        // Pass the invocation directly to the method handler
        return methodHandler.invoke(null, method, method, arguments);
    }

}
