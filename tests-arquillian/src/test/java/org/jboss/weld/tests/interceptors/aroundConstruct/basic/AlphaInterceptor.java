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
package org.jboss.weld.tests.interceptors.aroundConstruct.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import javax.enterprise.inject.spi.BeanManager;
import javax.interceptor.AroundConstruct;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@SuppressWarnings("serial")
@Interceptor
@AlphaBinding
public class AlphaInterceptor extends AbstractInterceptor implements Serializable {

    @AroundConstruct
    public void aroundConstruct(InvocationContext ctx) {
        try {
            invoked();

            checkConstructor(ctx, Alpha.class);
            assertNull(ctx.getMethod());
            assertNull(ctx.getTarget());

            assertEquals(1, ctx.getParameters().length);
            assertTrue(ctx.getParameters()[0] instanceof BeanManager);

            assertNull(ctx.proceed());

            checkConstructor(ctx, Alpha.class);
            assertNull(ctx.getMethod());
            assertNotNull(ctx.getTarget());
            assertTrue(ctx.getTarget() instanceof Alpha);

            assertEquals(1, ctx.getParameters().length);
            assertTrue(ctx.getParameters()[0] instanceof BeanManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
