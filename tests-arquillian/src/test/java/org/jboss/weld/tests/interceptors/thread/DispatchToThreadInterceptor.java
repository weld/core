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
package org.jboss.weld.tests.interceptors.thread;

import java.util.concurrent.Future;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Verifies that interception works fine if an interceptor dispatches the invocation to a different thread and the rest of the
 * chain is executed there.
 *
 * @author Jozef Hartinger
 *
 */
@Interceptor
@DispatchToThread
@Priority(Interceptor.Priority.APPLICATION)
public class DispatchToThreadInterceptor {

    @Inject
    private ThreadPool pool;

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        Future<Object> result = pool.submit(ctx);
        return result.get();
    }
}
