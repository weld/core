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

import static org.jboss.weld.util.collections.WeldCollections.immutableSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.weld.Container;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.collections.Arrays2;

import com.google.common.collect.Sets;

public abstract class AbstractBuiltInBean<T> extends RIBean<T> {

    private static final String ID_PREFIX = "Built-in";
    private boolean proxyRequired;
    private final Class<T> type;

    protected AbstractBuiltInBean(String idSuffix, BeanManagerImpl beanManager, Class<T> type) {
        super(new BuiltInBeanAttributes<T>(type), new StringBuilder().append(ID_PREFIX).append(BEAN_ID_SEPARATOR).append(idSuffix).toString(), beanManager);
        this.type = type;
    }

    @Override
    public void preInitialize() {
    }

    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        if (getScope() != null) {
            proxyRequired = Container.instance().services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
        } else {
            proxyRequired = false;
        }
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
    public RIBean<?> getSpecializedBean() {
        return null;
    }

    @Override
    public Set<WeldInjectionPoint<?, ?>> getWeldInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isSpecializing() {
        return false;
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

    protected static class BuiltInBeanAttributes<T> extends ImmutableBeanAttributes<T> {

        private static final Set<Annotation> DEFAULT_QUALIFIERS = Arrays2.asSet(DefaultLiteral.INSTANCE, AnyLiteral.INSTANCE);

        public BuiltInBeanAttributes(Class<T> type) {
            super(true, Collections.<Class<? extends Annotation>> emptySet(), false, null, DEFAULT_QUALIFIERS, immutableSet(Sets.<Type> newHashSet(type, Object.class)), Dependent.class);
        }
    }

}
