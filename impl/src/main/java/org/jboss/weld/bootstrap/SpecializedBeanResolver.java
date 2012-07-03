/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.ProducerMethod;

import com.google.common.collect.Iterables;

/**
 * Provides operations for resolving specialized beans. Since such resolutions are required during bootstrap when
 * {@link BeanManager}s are not initialized yet, this resolver operates on a set of accessible {@link BeanDeployerEnvironment}s.
 *
 * @author Jozef Hartinger
 *
 */
public class SpecializedBeanResolver {

    private final Set<BeanDeployerEnvironment> accessibleEnvironments;

    public SpecializedBeanResolver(Set<BeanDeployerEnvironment> accessibleEnvironments) {
        this.accessibleEnvironments = accessibleEnvironments;
    }

    private static interface BootstrapTransform<T extends Bean<?>> {
        Iterable<T> transform(T bean, BeanDeployerEnvironment environment);
    }

    private static final BootstrapTransform<AbstractClassBean<?>> CLASS_BEAN_TRANSFORM = new BootstrapTransform<AbstractClassBean<?>>() {
        @Override
        public Iterable<AbstractClassBean<?>> transform(AbstractClassBean<?> bean, BeanDeployerEnvironment environment) {
            return environment.getClassBeans(bean.getBeanClass().getSuperclass());
        }
    };

    private static final BootstrapTransform<ProducerMethod<?, ?>> PRODUCER_METHOD_TRANSFORM = new BootstrapTransform<ProducerMethod<?, ?>>() {
        @Override
        public Iterable<ProducerMethod<?, ?>> transform(ProducerMethod<?, ?> bean, BeanDeployerEnvironment environment) {
            return environment.getProducerMethod(bean.getBeanClass().getSuperclass(), bean.getEnhancedAnnotated().getSignature());
        }
    };

    private <T extends Bean<?>> Set<T> getSpecializedBeans(T bean, BootstrapTransform<T> transform) {
        Set<T> beans = new HashSet<T>();
        for (BeanDeployerEnvironment environment : accessibleEnvironments) {
            Iterables.addAll(beans, transform.transform(bean, environment));
        }
        return beans;
    }

    protected Set<AbstractClassBean<?>> resolveSpecializedBeans(final AbstractClassBean<?> bean) {
        if (!bean.isSpecializing()) {
            throw new IllegalArgumentException(bean + " is not a specializing bean");
        }
        return getSpecializedBeans(bean, CLASS_BEAN_TRANSFORM);
    }

    protected Set<ProducerMethod<?, ?>> resolveSpecializedBeans(final ProducerMethod<?, ?> bean) {
        if (!bean.isSpecializing()) {
            throw new IllegalArgumentException(bean + " is not a specializing bean");
        }
        return getSpecializedBeans(bean, PRODUCER_METHOD_TRANSFORM);
    }
}
