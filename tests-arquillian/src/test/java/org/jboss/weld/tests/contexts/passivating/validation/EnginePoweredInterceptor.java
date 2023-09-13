/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.passivating.validation;

import java.io.Serializable;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * This interceptor class is passivation capable but it has non-passivation capable dependencies. This is allowed provided it
 * does not intercept a bean declaring passivation scope.
 *
 * @author Jozef Hartinger
 *
 */
@Interceptor
@EnginePowered
@SuppressWarnings("serial")
public class EnginePoweredInterceptor implements Serializable {

    @Inject
    private Engine engine;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        engine.start();
        return ctx.proceed();
    }

}
