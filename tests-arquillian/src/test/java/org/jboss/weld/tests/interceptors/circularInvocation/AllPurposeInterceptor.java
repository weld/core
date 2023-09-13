/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.tests.interceptors.circularInvocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * @author Marius Bogoevici
 */
@Interceptor
@AllPurpose
public class AllPurposeInterceptor {
    public static List<Method> interceptedMethods = new ArrayList<Method>();

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        interceptedMethods.add(invocationContext.getMethod());
        return invocationContext.proceed();
    }
}
