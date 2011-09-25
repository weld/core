/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.introspector;

import org.jboss.weld.resources.SharedObjectFacade;
import org.jboss.weld.util.LazyValueHolder;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * {@link LazyValueHolder} that calculates a type closue. In many cases this
 * will not be needed, so computing it on demand saves memory and startup time.
 *
 * @author Stuart Douglas
 */
public class TypeClosureLazyValueHolder extends LazyValueHolder<Set<Type>> {

    private final Type type;

    private final Set<Type> types;

    private final String contextId;

    public TypeClosureLazyValueHolder(String contextId, Type type) {
        this.type = type;
        this.types = null;
        this.contextId = contextId;
    }

    public TypeClosureLazyValueHolder(String contextId, Set<Type> types) {
        this.type = null;
        this.types = types;
        this.contextId = contextId;
    }

    @Override
    protected Set<Type> computeValue() {
        if (types != null) {
            return types;
        }
        return SharedObjectFacade.getTypeClosure(contextId, type);
    }

}
