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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.util.AnnotationLiteral;

public class WrongExtension implements Extension {

    private Bean<Foo> fooBean;
    private InjectionPoint injectionPoint;

    public void observeBeforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        testAbdMethods(beanManager);
    }

    public void observeProcessBean(@Observes ProcessBean<Foo> event, BeanManager beanManager) {
        this.fooBean = event.getBean();
        testAbdMethods(beanManager);
    }

    public void observeAfterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        testAbvMethods(beanManager);
    }

    @SuppressWarnings({ "serial" })
    private void testAbdMethods(BeanManager beanManager) {

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
            beanManager.resolveDecorators(new HashSet<Type>(Collections.singletonList(Foo.class)));
            fail("resolveDecorators() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }

        try {
            beanManager.validate(new FooInjectionPoint());
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

        testAbvMethods(beanManager);
    }

    private void testAbvMethods(BeanManager beanManager) {
        if (fooBean != null) {
            try {
                beanManager.getReference(fooBean, Foo.class, null);
                fail("getReference() must not be available");
            } catch (IllegalStateException e) {
                // Expected
            }
        }
        try {
            beanManager.getInjectableReference(
                    beanManager.createInjectionPoint(beanManager.createAnnotatedType(Foo.class).getFields().iterator().next()),
                    null);
            fail("getInjectableReference() must not be available");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    void observeInjectionPoint(@Observes ProcessInjectionPoint<?, ?> event) {
        if (injectionPoint == null) {
            // simply store some IP which we'll try to validate later
            injectionPoint = event.getInjectionPoint();
        }
    }

    void validate(@Observes AfterDeploymentValidation event, BeanManager manager) {
        testAvailableMethods(manager);
    }

    private void testAvailableMethods(BeanManager beanManager) {
        beanManager.getReference(new FooBean(), Foo.class, beanManager.createCreationalContext(null));
        beanManager.getBeans("foo");
        beanManager.getBeans(Foo.class);
        beanManager.getInjectableReference(
                beanManager.createInjectionPoint(beanManager.createAnnotatedType(Foo.class).getFields().iterator().next()),
                beanManager.createCreationalContext(null));
        beanManager.resolve(null);
        beanManager.resolveObserverMethods(new Foo());
        beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE, new AnnotationLiteral<Transactional>() {
        });
        beanManager.resolveDecorators(new HashSet<Type>(Collections.singletonList(Foo.class)));
        beanManager.validate(injectionPoint);
        beanManager.getPassivationCapableBean("foo");
    }

    private static class FooBean implements Bean<Foo> {
        @Override
        public Class<?> getBeanClass() {
            return Foo.class;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        @Override
        public Set<Type> getTypes() {
            HashSet<Type> set = new HashSet<Type>();
            set.add(Foo.class);
            return set;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return Collections.emptySet();
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
        public Foo create(CreationalContext<Foo> creationalContext) {
            return null;
        }

        @Override
        public void destroy(Foo instance, CreationalContext<Foo> creationalContext) {
        }
    }

    @Vetoed
    private static class FooInjectionPoint implements InjectionPoint {

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
    }
}
