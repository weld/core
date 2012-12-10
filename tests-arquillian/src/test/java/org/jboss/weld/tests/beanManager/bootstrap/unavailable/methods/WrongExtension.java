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
package org.jboss.weld.tests.beanManager.bootstrap.unavailable.methods;

import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.util.AnnotationLiteral;

public class WrongExtension implements Extension {

    private Bean<Foo> fooBean;

    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        testUnavailableMethods(beanManager);
    }

    public void observeProcessBean(ProcessBean<Foo> event, BeanManager beanManager) {
        this.fooBean = event.getBean();
        testUnavailableMethods(beanManager);
    }

    public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        testUnavailableMethods(beanManager);
    }

    @SuppressWarnings({ "serial", "unchecked" })
    private void testUnavailableMethods(BeanManager beanManager) {

        if (fooBean != null) {
            try {
                beanManager.getReference(fooBean, Foo.class, null);
                fail("getReference() must not be available");
            } catch (IllegalStateException e) {
                // Expected
            }
        }

        try {
            beanManager.getBeans("foo");
            fail("getBeans() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.getBeans(Foo.class);
            fail("getBeans() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.getInjectableReference(
                    beanManager.createInjectionPoint(beanManager.createAnnotatedType(Foo.class).getFields().iterator().next()),
                    null);
            fail("getInjectableReference() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.resolve(null);
            fail("resolve() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.resolveObserverMethods(new Foo());
            fail("resolveObserverMethods() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new AnnotationLiteral<Transactional>() {
            });
            fail("resolveInterceptors() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.resolveDecorators(new HashSet<Type>(Arrays.asList(Foo.class)));
            fail("resolveDecorators() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.validate(new InjectionPoint() {

                @Override
                public boolean isTransient() {
                    return false;
                }

                @Override
                public boolean isDelegate() {
                    return false;
                }

                @Override
                public Type getType() {
                    return Foo.class;
                }

                @Override
                public Set<Annotation> getQualifiers() {
                    return null;
                }

                @Override
                public Member getMember() {
                    return null;
                }

                @Override
                public Bean<?> getBean() {
                    return null;
                }

                @Override
                public Annotated getAnnotated() {
                    return null;
                }
            });
            fail("validate() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.getPassivationCapableBean("foo");
            fail("getPassivationCapableBean() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

    }

}
