/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.bootstrap.index.processAnnotatedType.type;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class ProcessAnnotatedTypeObservers implements Extension {

    /*
     * Actual type
     */
    void o001(@Observes ProcessAnnotatedType<Alpha1Interface> event) {
    }

    void o002(@Observes ProcessAnnotatedType<AlphaAbstract> event) {
    }

    void o003(@Observes ProcessAnnotatedType<AlphaImpl> event) {
    }

    void o004(@Observes ProcessAnnotatedType<Object> event) {
    }

    void o005(@Observes ProcessAnnotatedType<Alpha2Interface> event) {
    }

    void o006(@Observes ProcessAnnotatedType<Alpha2Interface<?>> event) {
    }

    void o007(@Observes ProcessAnnotatedType<Alpha2Interface<Number>> event) {
    }

    void o008(@Observes ProcessAnnotatedType<Alpha2Interface<String>> event) {
    }

    void o009(@Observes ProcessAnnotatedType<Alpha2Interface<? extends Number>> event) {
    }

    /*
     * Type variables
     */
    <T extends Alpha1Interface> void o101(@Observes ProcessAnnotatedType<T> event) {
    }

    <T extends AlphaAbstract> void o102(@Observes ProcessAnnotatedType<T> event) {
    }

    <T extends AlphaImpl> void o103(@Observes ProcessAnnotatedType<T> event) {
    }

    <T extends Object> void o104(@Observes ProcessAnnotatedType<T> event) {
    }

    <T> void o15(@Observes ProcessAnnotatedType<T> event) {
    }

    /*
     * Wildcards
     */
    void o201(@Observes ProcessAnnotatedType<? extends Alpha1Interface> event) {
    }

    void o202(@Observes ProcessAnnotatedType<? extends AlphaAbstract> event) {
    }

    void o203(@Observes ProcessAnnotatedType<? extends AlphaImpl> event) {
    }

    void o204(@Observes ProcessAnnotatedType<? extends Object> event) {
    }

    void o205(@Observes ProcessAnnotatedType<?> event) {
    }

    <T> void o401(@Observes T event) {
    }

    <T extends ProcessAnnotatedType<Alpha1Interface>> void o402(@Observes T event) {
    }

    <T extends ProcessAnnotatedType<AlphaAbstract>> void o403(@Observes T event) {
    }

    <T extends ProcessAnnotatedType<AlphaImpl>> void o404(@Observes T event) {
    }

    <T extends ProcessAnnotatedType<Object>> void o405(@Observes T event) {
    }

    <T extends ProcessAnnotatedType<Alpha2Interface>> void o406(@Observes T event) {
    }
}
