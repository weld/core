/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

import java.util.Comparator;

/**
 * This comparator sorts classes alphabetically based on {@link Class#getName()} with notable difference that all
 * classes starting with {@code java.*} or {@code javax.*} come <b>after</b> all other classes.
 *
 * E.g. a set of these classes {javax.bar.Baz, java.something.Foo, bar.baz.Quax, mypackage.indeed.SomeBean} would be sorted
 * in the following manner - {bar.baz.Quax, mypackage.indeed.SomeBean, java.something.Foo, javax.bar.Baz}.
 */
public class CustomClassComparator implements Comparator<Class<?>> {

    private final String javaPrefix = "java.";
    private final String javaxPrefix = "javax.";

    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        String firstClassName = o1.getName();
        String secondClassName = o2.getName();
        // if no class starts with java.* or javax.* or if both start with it, perform standard comparison
        // if only one starts with this prefix, it goes later
        boolean firstClassHasJavaPrefix = firstClassName.startsWith(javaPrefix) || firstClassName.startsWith(javaxPrefix);
        boolean secondClassHasJavaPrefix = secondClassName.startsWith(javaPrefix) || secondClassName.startsWith(javaxPrefix);
        if (firstClassHasJavaPrefix) {
            if (secondClassHasJavaPrefix) {
                // both classes prefixed
                return firstClassName.compareTo(secondClassName);
            } else {
                // first class prefixed, second class not
                return 1;
            }
        } else {
            if (secondClassHasJavaPrefix) {
                // first class is not prefixed, second class is
                return -1;
            } else {
                // neither class is prefixed
                return firstClassName.compareTo(secondClassName);
            }
        }
    }
}
