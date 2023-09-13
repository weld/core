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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.jboss.weld.util.Preconditions;

/**
 * Weld's immutable {@link List} implementations. Based on the size of the data, methods of this class may return {@link List}
 * instances using various storage
 * strategies in order to achieve the best performance / memory consumption balance.
 *
 * @author Jozef Hartinger
 * @see WELD-1753
 *
 * @param <E> the element type
 */
public abstract class ImmutableList<E> extends AbstractImmutableList<E> {

    ImmutableList() {
    }

    /**
     * Creates an immutable list that consists of the given elements. This method should only be used in the varargs form. If
     * there is a need to create
     * an immutable list of an array of elements, {@link #copyOf(Object[])} should be used instead.
     *
     * @param elements the given elements
     * @return an immutable list
     */
    @SafeVarargs
    public static <T> List<T> of(T... elements) {
        Preconditions.checkNotNull(elements);
        return ofInternal(elements);
    }

    /**
     * Creates an immutable list that consists of the elements in the given array. A copy of the given array is used which means
     * that any modifications to the given array will not affect the immutable list.
     *
     * @param elements the given array of elements
     * @return an immutable list
     */
    public static <T> List<T> copyOf(T[] elements) {
        Preconditions.checkNotNull(elements);
        return ofInternal(elements.clone());
    }

    /**
     * Creates an immutable list that consists of the elements in the given collection. If the given collection is already an
     * immutable list,
     * it is returned directly.
     *
     * @param source the given collection
     * @return an immutable list
     */
    public static <T> List<T> copyOf(Collection<T> source) {
        Preconditions.checkNotNull(source);
        if (source instanceof ImmutableList<?>) {
            return (ImmutableList<T>) source;
        }
        if (source.isEmpty()) {
            return Collections.emptyList();
        }
        return ofInternal(source.toArray());
    }

    public static <T> List<T> copyOf(Iterable<T> source) {
        Preconditions.checkNotNull(source);
        if (source instanceof Collection<?>) {
            return copyOf((Collection<T>) source);
        }
        return ImmutableList.<T> builder().addAll(source).build();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> ofInternal(Object[] elements) {
        switch (elements.length) {
            case 0:
                return Collections.emptyList();
            case 1:
                return new ImmutableTinyList.Singleton<T>((T) elements[0]);
            default:
                return new ImmutableArrayList<T>(checkElementsNotNull(elements));
        }
    }

    private static Object[] checkElementsNotNull(Object[] objects) {
        for (Object object : objects) {
            Preconditions.checkNotNull(object);
        }
        return objects;
    }

    /**
     * Returns a collector that can be used to collect items of a stream into an immutable list.
     *
     * @return collector
     */
    @SuppressWarnings("unchecked")
    public static <T> ImmutableListCollector<T> collector() {
        return (ImmutableListCollector<T>) ImmutableListCollector.INSTANCE;
    }

    /**
     * Creates a new empty builder for building immutable lists.
     *
     * @return a new empty builder
     */
    public static <T> Builder<T> builder() {
        return new BuilderImpl<T>();
    }

    public interface Builder<T> {

        Builder<T> add(T item);

        Builder<T> addAll(Iterable<? extends T> items);

        Builder<T> addAll(@SuppressWarnings("unchecked") T... items);

        List<T> build();
    }

    private static class BuilderImpl<T> implements Builder<T> {

        private static final int DEFAULT_CAPACITY = 10;

        private List<T> list;

        private BuilderImpl() {
            this.list = new ArrayList<>(DEFAULT_CAPACITY);
        }

        @Override
        public Builder<T> add(T item) {
            if (item == null) {
                throw new IllegalArgumentException("This collection does not support null values");
            }
            list.add(item);
            return this;
        }

        @Override
        public Builder<T> addAll(@SuppressWarnings("unchecked") T... items) {
            for (T item : items) {
                add(item);
            }
            return this;
        }

        @Override
        public Builder<T> addAll(Iterable<? extends T> items) {
            for (T item : items) {
                add(item);
            }
            return this;
        }

        BuilderImpl<T> addAll(BuilderImpl<T> items) {
            addAll(items.list);
            return this;
        }

        @Override
        public List<T> build() {
            return ImmutableList.ofInternal(list.toArray());
        }
    }

    private static class ImmutableListCollector<T> implements Collector<T, BuilderImpl<T>, List<T>> {

        private static final ImmutableListCollector<Object> INSTANCE = new ImmutableListCollector<>();
        private static final Set<Characteristics> CHARACTERISTICS = ImmutableSet.of();

        @Override
        public Supplier<BuilderImpl<T>> supplier() {
            return BuilderImpl::new;
        }

        @Override
        public BiConsumer<BuilderImpl<T>, T> accumulator() {
            return BuilderImpl::add;
        }

        @Override
        public BinaryOperator<BuilderImpl<T>> combiner() {
            return (builder1, builder2) -> builder1.addAll(builder2);
        }

        @Override
        public Function<BuilderImpl<T>, List<T>> finisher() {
            return BuilderImpl::build;
        }

        @Override
        public Set<java.util.stream.Collector.Characteristics> characteristics() {
            return CHARACTERISTICS;
        }
    }
}
