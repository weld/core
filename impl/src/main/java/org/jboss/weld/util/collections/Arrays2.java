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
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pmuir
 */
public class Arrays2 {

    public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
    public static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    private Arrays2() {
    }

    public static final boolean containsAll(Object[] array, Object... values) {
        return Arrays.asList(array).containsAll(Arrays.asList(values));
    }

    public static final boolean unorderedEquals(Object[] array, Object... values) {
        return containsAll(array, values) && array.length == values.length;
    }

    public static <T> Set<T> asSet(T... array) {
        Set<T> result = new HashSet<T>(array.length);
        for (T a : array) {
            result.add(a);
        }
        return result;
    }

    // Cloning
    /**
    * Copies the specified array, truncating or padding with nulls (if necessary)
    * so the copy has the specified length.  For all indices that are
    * valid in both the original array and the copy, the two arrays will
    * contain identical values.  For any indices that are valid in the
    * copy but not the original, the copy will contain <tt>null</tt>.
    * Such indices will exist if and only if the specified length
    * is greater than that of the original array.
    * The resulting array is of exactly the same class as the original array.
    *
    * @param original the array to be copied
    * @param newLength the length of the copy to be returned
    * @return a copy of the original array, truncated or padded with nulls
    *     to obtain the specified length
    * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
    * @throws NullPointerException if <tt>original</tt> is null
    * @since 1.6
    */
   @SuppressWarnings("unchecked")
   public static <T> T[] copyOf(T[] original, int newLength) {
       return (T[]) copyOf(original, newLength, original.getClass());
   }

    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>null</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     * The resulting array is of the class <tt>newType</tt>.
     *
     * @param original  the array to be copied
     * @param newLength the length of the copy to be returned
     * @param newType   the class of the copy to be returned
     * @return a copy of the original array, truncated or padded with nulls
     *         to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException       if <tt>original</tt> is null
     * @throws ArrayStoreException        if an element copied from
     *                                    <tt>original</tt> is not of a runtime type that can be stored in
     *                                    an array of class <tt>newType</tt>
     * @since 1.6
     */
    public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        @SuppressWarnings("unchecked")
        T[] copy = ((Object) newType == (Object) Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                Math.min(original.length, newLength));
        return copy;
    }

}
