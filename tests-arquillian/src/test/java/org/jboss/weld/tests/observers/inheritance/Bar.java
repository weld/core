/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.observers.inheritance;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Specializes;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Alternative
@Specializes
public class Bar extends Foo {
    @Override
    void init() {
        System.out.println("INIT BAR");
    }

    @Override
    void onEvent(@Observes @SomeQualifier MyEvent e) {
        System.out.println("EVENT BAR");
        throw new RuntimeException("Should not be called!");
    }
}