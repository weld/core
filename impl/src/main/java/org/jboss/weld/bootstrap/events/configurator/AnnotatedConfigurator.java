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
package org.jboss.weld.bootstrap.events.configurator;

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import javax.enterprise.inject.spi.Annotated;

/**
 * An abstract configurator for all annotated elements.
 *
 * @author Martin Kouba
 *
 * @param <T>
 * @param <A>
 * @param <C>
 */
abstract class AnnotatedConfigurator<T, A extends Annotated, C extends AnnotatedConfigurator<T, A, C>> {

    private final A annotated;

    private final Set<Annotation> annotations;

    /**
     *
     * @param annotated
     */
    AnnotatedConfigurator(A annotated) {
        this.annotated = annotated;
        this.annotations = new HashSet<>(annotated.getAnnotations());
    }

    public A getAnnotated() {
        return annotated;
    }

    public C add(Annotation annotation) {
        checkArgumentNotNull(annotation);
        annotations.add(annotation);
        return self();
    }

    public C remove(Predicate<Annotation> predicate) {
        checkArgumentNotNull(predicate);
        for (Iterator<Annotation> iterator = annotations.iterator(); iterator.hasNext();) {
            if (predicate.test(iterator.next())) {
                iterator.remove();
            }
        }
        return self();
    }

    public C removeAll() {
        annotations.clear();
        return self();
    }

    protected abstract C self();

    Set<Annotation> getAnnotations() {
        return annotations;
    }

}
