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
package org.jboss.weld.tests.interceptors.extension;

import java.io.Serializable;

import jakarta.interceptor.InvocationContext;

/**
 * This is not an interceptor (it misses required annotations). The CustomInterceptor delegates to instances of this class.
 *
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 *
 */
@SuppressWarnings("serial")
public class FooInterceptor implements Serializable {

    private static boolean invoked = false;

    Object intercept(InvocationContext ctx) throws Exception {
        invoked = true;
        return ctx.proceed();
    }

    public static boolean isInvoked() {
        return invoked;
    }

    public static void reset() {
        invoked = false;
    }
}
