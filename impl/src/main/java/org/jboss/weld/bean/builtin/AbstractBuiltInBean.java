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

import org.jboss.weld.Container;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.collections.Arrays2;

import javax.enterprise.context.Dependent;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

public abstract class AbstractBuiltInBean<T> extends RIBean<T> {

    private static final String ID_PREFIX = "Built-in";
    private static final Set<Annotation> DEFAULT_QUALIFIERS = Arrays2.asSet(DefaultLiteral.INSTANCE, AnyLiteral.INSTANCE);
    private boolean proxyRequired;

    protected AbstractBuiltInBean(String idSuffix, BeanManagerImpl beanManager) {
        super(new StringBuilder().append(ID_PREFIX).append(BEAN_ID_SEPARATOR).append(idSuffix).toString(), beanManager);
    }

    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        if (getScope() != null) {
            proxyRequired = Container.instance(getBeanManager().getContextId()).services().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal();
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

    public Set<Annotation> getQualifiers() {
        return DEFAULT_QUALIFIERS;
    }

    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public RIBean<?> getSpecializedBean() {
        return null;
    }

    public String getName() {
        return null;
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public Set<WeldInjectionPoint<?, ?>> getWeldInjectionPoints() {
        return Collections.emptySet();
    }

    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isSpecializing() {
        return false;
    }

    public boolean isAlternative() {
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

}
