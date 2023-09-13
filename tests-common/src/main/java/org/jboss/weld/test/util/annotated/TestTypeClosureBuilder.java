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
package org.jboss.weld.test.util.annotated;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A type closure builder
 *
 * @author Stuart Douglas
 */
class TestTypeClosureBuilder {

    final Set<Type> types = new HashSet<Type>();

    public TestTypeClosureBuilder add(Class<?> beanType) {
        Class<?> c = beanType;
        do {
            types.add(c);
            c = c.getSuperclass();
        } while (c != null);
        Collections.addAll(types, beanType.getInterfaces());
        return this;
    }

    public TestTypeClosureBuilder addInterfaces(Class<?> beanType) {
        Collections.addAll(types, beanType.getInterfaces());
        return this;
    }

    public Set<Type> getTypes() {
        return types;
    }

}
