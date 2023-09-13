/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.tests.interceptors.binding.inheritance;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

public class Interceptors {

    public static abstract class AbstractInterceptor {
        private int inc;

        public AbstractInterceptor(int inc) {
            this.inc = inc;
        }

        @AroundInvoke
        public Object intercept(InvocationContext ctx) throws Exception {
            Object result = ctx.proceed();
            if (result instanceof Integer) {
                return (Integer) result + inc;
            }
            return result;
        }
    }

    @Interceptor
    @Binding(Binding.Type.ALPHA)
    public static class AlphaInterceptor extends AbstractInterceptor {
        public AlphaInterceptor() {
            super(1);
        }
    }

    @Interceptor
    @Binding(Binding.Type.BRAVO)
    public static class BravoInterceptor extends AbstractInterceptor {
        public BravoInterceptor() {
            super(2);
        }
    }

    @Interceptor
    @Binding(Binding.Type.CHARLIE)
    public static class CharlieInterceptor extends AbstractInterceptor {
        public CharlieInterceptor() {
            super(3);
        }
    }

    @Interceptor
    @Binding(Binding.Type.DELTA)
    public static class DeltaInterceptor extends AbstractInterceptor {
        public DeltaInterceptor() {
            super(4);
        }
    }

    @Interceptor
    @Binding(Binding.Type.ECHO)
    public static class EchoInterceptor extends AbstractInterceptor {
        public EchoInterceptor() {
            super(5);
        }
    }

}
