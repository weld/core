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
package org.jboss.weld.util;

import com.google.common.base.Function;

/**
 *
 * @author Martin Kouba
 */
public final class Functions {

    private Functions() {
    }

    /**
     * TODO temporary workaround - should be removed once WELD-1754 is resolved.
     *
     * @param jdkFunction
     * @return a Guava function based on an existing JDK8 function instance
     */
    public static <T, R> Function<T, R> toGuavaFunction(java.util.function.Function<T, R> jdkFunction) {
        return new JdkToGuavaFunction<>(jdkFunction);
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <T>
     * @param <R>
     */
    private static class JdkToGuavaFunction<T, R> implements Function<T, R>{

        private final java.util.function.Function<T, R> jdkFunction;

        public JdkToGuavaFunction(java.util.function.Function<T, R> jdkFunction) {
            this.jdkFunction = jdkFunction;
        }

        @Override
        public R apply(T input) {
            return jdkFunction.apply(input);
        }

    }

}
