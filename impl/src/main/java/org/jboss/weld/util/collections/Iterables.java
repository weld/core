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
package org.jboss.weld.util.collections;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

import java.util.Iterator;

import com.google.common.base.Function;

/**
 * Static utility methods for {@link Iterable}.
 *
 * @author Martin Kouba
 */
public final class Iterables {

    private Iterables() {
    }

    /**
     *
     * @param iterable
     * @return
     * @see WeldCollections#toMultiRowString(java.util.Collection)
     */
    public static <T> String toMultiRowString(Iterable<T> iterable) {
        StringBuilder builder = new StringBuilder("\n  - ");
        for (Iterator<T> iterator = iterable.iterator(); iterator.hasNext();) {
            T element = iterator.next();
            builder.append(element);
            if (iterator.hasNext()) {
                builder.append(",\n  - ");
            }
        }
        return builder.toString();
    }

    public static <F, T> Iterable<T> flatMap(Iterable<F> iterable, Function<F, Iterable<T>> function) {
        return concat(transform(iterable, function));
    }
}
