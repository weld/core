/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.lifecycle.processSyntheticAnnotatedType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;

public class VerifyingExtension implements Extension {

    private Set<Class<?>> patClasses = new HashSet<Class<?>>();
    private Set<Class<?>> psatClasses = new HashSet<Class<?>>();
    private Map<Class<?>, Extension> sources = new HashMap<Class<?>, Extension>();

    <T> void verify(@Observes ProcessAnnotatedType<T> event) {
        if (event instanceof ProcessSyntheticAnnotatedType<?>) {
            psatClasses.add(event.getAnnotatedType().getJavaClass());
        } else {
            patClasses.add(event.getAnnotatedType().getJavaClass());
        }
    }

    <T> void verifySource(@Observes ProcessSyntheticAnnotatedType<T> event) {
        sources.put(event.getAnnotatedType().getJavaClass(), event.getSource());
    }

    protected Set<Class<?>> getPatClasses() {
        return patClasses;
    }

    protected Set<Class<?>> getPsatClasses() {
        return psatClasses;
    }

    protected Map<Class<?>, Extension> getSources() {
        return sources;
    }
}
