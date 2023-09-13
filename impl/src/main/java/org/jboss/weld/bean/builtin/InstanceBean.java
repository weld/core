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
package org.jboss.weld.bean.builtin;

import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Provider;

import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

public class InstanceBean extends AbstractFacadeBean<Instance<?>> {
    private static final Type INSTANCE_TYPE = new TypeLiteral<Instance<Object>>() {
        private static final long serialVersionUID = -1246199714407637856L;
    }.getType();
    private static final Type PROVIDER_TYPE = new TypeLiteral<Provider<Object>>() {
        private static final long serialVersionUID = -5256050387550468441L;
    }.getType();
    private static final Type WELD_INSTANCE_TYPE = new TypeLiteral<WeldInstance<Object>>() {
        private static final long serialVersionUID = -1246199714407637856L;
    }.getType();
    private static final Set<Type> DEFAULT_TYPES = Arrays2.<Type> asSet(INSTANCE_TYPE, WELD_INSTANCE_TYPE, PROVIDER_TYPE,
            Object.class);

    public InstanceBean(BeanManagerImpl manager) {
        super(manager, Reflections.<Class<Instance<?>>> cast(Instance.class));
    }

    @Override
    public Class<?> getBeanClass() {
        return InstanceImpl.class;
    }

    public Set<Type> getTypes() {
        return DEFAULT_TYPES;
    }

    @Override
    protected Instance<?> newInstance(InjectionPoint injectionPoint, CreationalContext<Instance<?>> creationalContext) {
        return InstanceImpl.of(injectionPoint, creationalContext, getBeanManager());
    }

    @Override
    public String toString() {
        return "Implicit Bean [jakarta.enterprise.inject.Instance] with qualifiers [@Default]";
    }

    @Override
    protected Type getDefaultType() {
        return INSTANCE_TYPE;
    }

    @Override
    public boolean isDependentContextOptimizationAllowed() {
        return false;
    }

}
