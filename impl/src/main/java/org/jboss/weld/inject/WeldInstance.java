/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.inject;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;

/**
 * TODO move to Weld API
 *
 * An enhanced version of {@link Instance}.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public interface WeldInstance<T> extends Instance<T> {

    /**
     * Obtains a contextual reference handler for the bean that has the required type and required qualifiers and is eligible for injection.
     * <p>
     * Note that each invocation of this method results in a separate {@link Instance#get()} invocation.
     *
     * @return a new handler
     * @throws UnsatisfiedResolutionException
     * @throws AmbiguousResolutionException
     */
    Handler<T> getHandler();

    /**
     *
     * @return <code>true</code> if satisfied and not ambiguous, <code>false</code> otherwise
     * @see #isAmbiguous()
     * @see #isUnsatisfied()
     */
    boolean isResolvable();

    /**
     * Returns an iterator over contextual reference handlers.
     *
     * @return an iterator
     */
    Iterator<Handler<T>> handlerIterator();

    @Override
    WeldInstance<T> select(Annotation... qualifiers);

    @Override
    <U extends T> WeldInstance<U> select(Class<U> subtype, Annotation... qualifiers);

    @Override
    <U extends T> WeldInstance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers);

    /**
     * A contextual reference handler. Not suitable for sharing between threads.
     * <p>
     * Holds the contextual reference and allows to inspect the metadata of the relevant bean and also to destroy the underlying contextual instance.
     *
     * @author Martin Kouba
     *
     * @param <T>
     */
    public interface Handler<T> extends AutoCloseable {

        /**
         *
         * @return the contextual reference
         * @see Instance#get()
         */
        T get();

        /**
         *
         * @return the bean metadata
         */
        Bean<?> getBean();

        /**
         * Destroy the contextual instance.
         * <p>
         * It's a no-op if called multiple times or if the producing {@link WeldInstance} is destroyed already.
         *
         * @see Instance#destroy(Object)
         */
        void destroy();

        /**
         * Delegates to {@link #destroy()}.
         */
        @Override
        void close();

    }

}
