/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.enterprise;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 *
 * @author Martin Kouba
 */
public class TestExtension implements Extension {

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {

        final BeanManagerImpl beanManagerImpl = BeanManagerProxy.unwrap(beanManager);

        event.addBean(new Bean<TestBean>() {

            @Override
            public TestBean create(CreationalContext<TestBean> creationalContext) {
                return new TestBean(beanManagerImpl.getId());
            }

            @Override
            public void destroy(TestBean instance, CreationalContext<TestBean> creationalContext) {
            }

            @Override
            public Set<Type> getTypes() {
                Set<Type> types = new HashSet<>();
                types.add(TestBean.class);
                types.add(Object.class);
                return types;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                Set<Annotation> qualifiers = new HashSet<>();
                qualifiers.add(Any.Literal.INSTANCE);
                qualifiers.add(Default.Literal.INSTANCE);
                return qualifiers;
            }

            @Override
            public Class<? extends Annotation> getScope() {
                return Dependent.class;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Set<Class<? extends Annotation>> getStereotypes() {
                return Collections.emptySet();
            }

            @Override
            public boolean isAlternative() {
                return false;
            }

            @Override
            public Class<?> getBeanClass() {
                return TestBean.class;
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.emptySet();
            }
        });

    }

}
