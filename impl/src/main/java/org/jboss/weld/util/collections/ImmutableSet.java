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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.jboss.weld.util.Preconditions;

/**
 * Weld's immutable set implementation. Instances returned from methods of this class may use different strategies to achieve
 * good performance / memory
 * consumption balance.
 * <p>
 * These strategies include:
 * <ul>
 * <li>A single shared {@link Set} implementation instance representing an empty list</li>
 * <li>An optimized implementation for holding one, two or three references.</li>
 * <li>An immutable {@link Set} implementation based on hashing</li>
 * </ul>
 * <p/>
 *
 * @author Jozef Hartinger
 * @ee WELD-1753
 *
 * @param <T> the type of elements
 */
public abstract class ImmutableSet<T> extends AbstractImmutableSet<T> {

    ImmutableSet() {
    }

    /**
     * Creates a new immutable set that consists of the elements in the given collection. If the given collection is already an
     * instance created by
     * {@link ImmutableSet}, the instance is re-used.
     *
     * @param collection the given collection
     * @return a new immutable set that consists of the elements in the given collection
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> copyOf(Collection<? extends T> collection) {
        Preconditions.checkNotNull(collection);
        if (collection instanceof AbstractImmutableSet<?>) {
            return (Set<T>) collection;
        }
        if (collection.isEmpty()) {
            return Collections.emptySet();
        }
        if (collection instanceof Set) {
            return from((Set<T>) collection);
        }
        return ImmutableSet.<T> builder().addAll(collection).build();
    }

    /**
     * Creates a new immutable set that consists of given elements.
     *
     * @param elements the given elements
     * @return a new immutable set that consists of given elements
     */
    @SafeVarargs
    public static <T> Set<T> of(T... elements) {
        Preconditions.checkNotNull(elements);
        return ImmutableSet.<T> builder().addAll(elements).build();
    }

    /**
     * Returns a collector that can be used to collect items of a stream into an immutable set.
     *
     * @return collector
     */
    @SuppressWarnings("unchecked")
    public static <T> ImmutableSetCollector<T> collector() {
        return (ImmutableSetCollector<T>) ImmutableSetCollector.INSTANCE;
    }

    /**
     * Creates a new empty builder for building immutable sets.
     *
     * @return a new empty builder
     */
    public static <T> Builder<T> builder() {
        return new BuilderImpl<T>();
    }

    /**
     * Builder for building immutable sets. The builder may be re-used after build() is called.
     *
     * @author Jozef Hartinger
     *
     * @param <T> the type of elements
     */
    public interface Builder<T> {

        Builder<T> add(T item);

        Builder<T> addAll(Iterable<? extends T> items);

        Builder<T> addAll(@SuppressWarnings("unchecked") T... items);

        /**
         * Create a new immutable set.
         *
         * @return a new immutable set
         */
        Set<T> build();
    }

    private static class BuilderImpl<T> implements Builder<T> {

        private Set<T> set;

        private BuilderImpl() {
            this.set = new LinkedHashSet<>();
        }

        @Override
        public Builder<T> add(T item) {
            if (item == null) {
                throw new IllegalArgumentException("This collection does not support null values");
            }
            set.add(item);
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
            addAll(items.set);
            return this;
        }

        @Override
        public Set<T> build() {
            return from(set);
        }
    }

    private static <T> Set<T> from(Set<T> set) {
        switch (set.size()) {
            case 0:
                return Collections.emptySet();
            case 1:
                return new ImmutableTinySet.Singleton<T>(set);
            case 2:
                return new ImmutableTinySet.Doubleton<T>(set);
            case 3:
                return new ImmutableTinySet.Tripleton<T>(set);
            default:
                return new ImmutableHashSet<>(set);
        }
    }

    private static class ImmutableSetCollector<T> implements Collector<T, BuilderImpl<T>, Set<T>> {

        private static final ImmutableSetCollector<Object> INSTANCE = new ImmutableSetCollector<>();
        private static final Set<Characteristics> CHARACTERISTICS = of(Characteristics.UNORDERED);

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
        public Function<BuilderImpl<T>, Set<T>> finisher() {
            return BuilderImpl::build;
        }

        @Override
        public Set<java.util.stream.Collector.Characteristics> characteristics() {
            return CHARACTERISTICS;
        }
    }
}
