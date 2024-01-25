/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.beans.afterbeandiscovery;

import static org.junit.Assert.assertEquals;

import java.util.function.Consumer;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.junit.Test;

public class AfterBeanDiscoveryAddBeanTest {

    static class Foo {
        public String tellMyType() {
            return getClass().getSimpleName();
        }
    }

    @Alternative
    static class FooAlternative extends Foo {
    }

    static class FooInjected {
        @Inject
        Foo foo;

        Foo getFoo() {
            return foo;
        }
    }

    static class AfterBeanDiscoveryAddFooInjectedExtension implements Extension {
        void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
            AnnotatedType<FooInjected> annotatedType = beanManager.createAnnotatedType(FooInjected.class);
            BeanAttributes<FooInjected> beanAttributes = beanManager.createBeanAttributes(annotatedType);
            InjectionTargetFactory<FooInjected> injectionTargetFactory = beanManager.getInjectionTargetFactory(annotatedType);
            Bean<FooInjected> bean = beanManager.createBean(beanAttributes, FooInjected.class, injectionTargetFactory);
            afterBeanDiscovery.addBean(bean);
        }
    }

    @Test
    public void testAlternatives() {
        // given Foo and enabled FooAlternative alternative for Foo, injection should work similarly
        // with respect to selecting the alternative FooAlternative for Foo, in both following cases:

        // added with addBeanClass to Weld (presumably covered elsewhere serves here to compare)
        testAlternatives(weld -> weld.addBeanClass(FooInjected.class));
        // added in an extension with @Observes AfterBeanDiscovery
        testAlternatives(weld -> weld.addExtension(new AfterBeanDiscoveryAddFooInjectedExtension()));
    }

    void testAlternatives(Consumer<Weld> weldModifier) {
        Weld weld = new Weld().disableDiscovery();
        weld.addBeanClass(Foo.class);
        weld.addBeanClass(FooAlternative.class);
        weld.addAlternative(FooAlternative.class);

        weldModifier.accept(weld);

        try (SeContainer container = weld.initialize()) {
            FooInjected fooInjected = container.select(FooInjected.class).get();
            assertEquals(FooAlternative.class.getSimpleName(), fooInjected.getFoo().tellMyType());
        }
    }

}
