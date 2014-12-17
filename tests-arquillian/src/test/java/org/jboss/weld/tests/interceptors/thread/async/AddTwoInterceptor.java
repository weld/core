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
package org.jboss.weld.tests.interceptors.thread.async;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@AddTwo
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AddTwoInterceptor {

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        if (ctx.getContextData().get(AddOneInterceptor.class.getName()) == null) {
            throw new IllegalStateException();
        }
        ctx.getContextData().put(AddTwoInterceptor.class.getName(), this);
        Object[] params = ctx.getParameters();
        Integer param = (Integer) params[0];
        params[0] = param + 2;
        return ctx.proceed();
    }
}
