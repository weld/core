/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.builtinBeans.metadata.broken.interceptor;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Intercepted;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 *
 * @author Martin Kouba
 */
@FooServletInterceptorBinding
@Priority(Interceptor.Priority.APPLICATION + 10)
@Interceptor
public class FooServletInterceptor {

    static boolean called = false;

    @Inject
    @Intercepted
    Bean<?> bean;

    @AroundInvoke
    public Object alwaysReturnThis(InvocationContext ctx) throws Exception {
        called = true;
        if (bean != null) {
            throw new IllegalStateException();
        }
        return ctx.proceed();
    }

}
