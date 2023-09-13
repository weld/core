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
package org.jboss.weld.tests.interceptors.aroundConstruct.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@CharlieBinding
@Interceptor
@Priority(2020)
public class CharlieInterceptor extends AbstractInterceptor {

    @AroundConstruct
    void aroundConstruct(InvocationContext ctx) {
        checkConstructor(ctx.getConstructor());

        Object[] params = ctx.getParameters();
        assertEquals(1, params.length);
        assertEquals(2, params[0]);
        ctx.setParameters(new Integer[] { 3 });

        // test context data before construction
        assertTrue(ctx.getContextData().containsKey(AlphaInterceptor.class.getName()));
        assertTrue(ctx.getContextData().containsKey(BravoInterceptor.class.getName()));
        assertTrue(ctx.getContextData().containsKey("foo"));
        assertEquals(BravoInterceptor.class.getName(), ctx.getContextData().get("foo"));
        ctx.getContextData().put(CharlieInterceptor.class.getName(), this);
        ctx.getContextData().put("foo", CharlieInterceptor.class.getName());

        proceed(ctx);

        // test context data after construction
        assertTrue(ctx.getContextData().containsKey(AlphaInterceptor.class.getName()));
        assertTrue(ctx.getContextData().containsKey(BravoInterceptor.class.getName()));
        assertTrue(ctx.getContextData().containsKey(CharlieInterceptor.class.getName()));
        assertTrue(ctx.getContextData().containsKey("foo"));
        assertEquals(CharlieInterceptor.class.getName(), ctx.getContextData().get("foo"));

        // test parameters after construction
        Object[] params2 = ctx.getParameters();
        assertEquals(1, params2.length);
        assertEquals(3, params2[0]);

        checkConstructor(ctx.getConstructor());
    }
}
