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
package org.jboss.weld.tests.extensions.alternative.metadata;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.TypeLiteral;

public class ModifyingExtension implements Extension {

    public void observeAlphaAnnotatedType(@Observes ProcessAnnotatedType<Alpha> event) {
        event.setAnnotatedType(new ForwardingAnnotatedType<Alpha>(event.getAnnotatedType()) {
            @Override
            public Set<Type> getTypeClosure() {
                Set<Type> closure = new HashSet<Type>();
                closure.add(AlphaInterface.class);
                // Don't add Delta interface
                return closure;
            }
        });
    }

    public void observeBravoAnnotatedType(@Observes ProcessAnnotatedType<Bravo> event) {
        event.setAnnotatedType(new ForwardingAnnotatedType<Bravo>(event.getAnnotatedType()) {
            @Override
            public Set<Type> getTypeClosure() {
                Set<Type> closure = new HashSet<Type>();
                closure.add(Bravo.class);
                closure.add(Charlie.class);
                // Don't add Alpha class
                return closure;
            }
        });
    }

    public void observeEchoAnnotatedType(@Observes ProcessAnnotatedType<Echo> event) {
        event.setAnnotatedType(new ForwardingAnnotatedType<Echo>(event.getAnnotatedType()) {
            @SuppressWarnings("serial")
            @Override
            public Set<Type> getTypeClosure() {
                Set<Type> closure = new HashSet<Type>();
                closure.add(new TypeLiteral<FoxtrotInterface<Integer>>() {
                }.getType());
                // Don't add EchoInterface class
                return closure;
            }
        });
    }

}
