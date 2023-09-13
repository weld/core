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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.collections.ImmutableSet;

public abstract class AbstractBuiltInBean<T> extends RIBean<T> {

    private boolean proxyRequired;
    private final Class<T> type;

    protected AbstractBuiltInBean(BeanIdentifier identifier, BeanManagerImpl beanManager, Class<T> type) {
        super(new BuiltInBeanAttributes<T>(type), identifier, beanManager);
        this.type = type;
    }

    @Override
    public void preInitialize() {
    }

    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        proxyRequired = getScope() != null && isNormalScoped();
    }

    @Override
    public void cleanupAfterBoot() {
        // No-op
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        // No-op
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isProxyable() {
        return true;
    }

    @Override
    public boolean isPassivationCapableBean() {
        return true;
    }

    @Override
    public boolean isPassivationCapableDependency() {
        return true;
    }

    @Override
    public boolean isProxyRequired() {
        return proxyRequired;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    public boolean isDependentContextOptimizationAllowed() {
        // By default, all dependent built-in beans do not have to be stored in a CreationalContext
        return Dependent.class.equals(getScope());
    }

    protected static class BuiltInBeanAttributes<T> extends ImmutableBeanAttributes<T> {

        public BuiltInBeanAttributes(Class<T> type) {
            super(Collections.<Class<? extends Annotation>> emptySet(), false, null, Bindings.DEFAULT_QUALIFIERS,
                    ImmutableSet.of(Object.class, type), Dependent.class);
        }
    }

}
