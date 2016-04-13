/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.collections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pmuir
 */
public final class Arrays2 {

    public static final Object[] EMPTY_ARRAY = new Object[0];
    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
    public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private Arrays2() {
    }

    public static boolean containsAll(Object[] array, Object... values) {
        return Arrays.asList(array).containsAll(Arrays.asList(values));
    }

    public static boolean unorderedEquals(Object[] array, Object... values) {
        return containsAll(array, values) && array.length == values.length;
    }

    @SafeVarargs
    public static <T> Set<T> asSet(T... array) {
        Set<T> result = new HashSet<T>(array.length);
        Collections.addAll(result, array);
        return result;
    }

    public static <T> boolean contains(T[] array, T value) {
        for (T element : array) {
            if (element.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
