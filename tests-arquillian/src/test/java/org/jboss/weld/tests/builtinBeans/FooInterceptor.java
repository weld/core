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
package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.Intercepted;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Foo
@SuppressWarnings("serial")
public class FooInterceptor implements Serializable {

    @Inject
    @SuppressWarnings("unused")
    private javax.enterprise.inject.spi.Interceptor<FooInterceptor> interceptor;

    @Inject
    @Intercepted
    @SuppressWarnings("unused")
    private Bean<?> bean;

    @AroundInvoke
    Object intercept(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}
