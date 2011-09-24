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

package org.jboss.weld.tests.interceptors.simple;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
@Interceptor
@PrimaryInterceptionBinding
public class SimpleInterceptor {
    public static boolean postConstructCalled = false;
    public static boolean aroundInvokeCalled = false;
    public static boolean preDestroyCalled = false;

    @PostConstruct
    public void doPostConstruct(InvocationContext context) throws Exception {
        postConstructCalled = true;
        context.proceed();
    }

    @AroundInvoke
    public Object doAround(InvocationContext context) throws Exception {
        aroundInvokeCalled = true;
        return context.proceed();
    }

    @PreDestroy
    public void doPreDestroy(InvocationContext context) throws Exception {
        preDestroyCalled = true;
        context.proceed();
    }
}
