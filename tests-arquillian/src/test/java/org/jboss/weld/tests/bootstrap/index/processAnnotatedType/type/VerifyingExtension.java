/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.bootstrap.index.processAnnotatedType.type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.bootstrap.FastProcessAnnotatedTypeResolver;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeEventResolvable;
import org.jboss.weld.bootstrap.events.RequiredAnnotationDiscovery;
import org.jboss.weld.event.GlobalObserverNotifierService;
import org.jboss.weld.event.ObserverNotifier;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.ClassFileServices;
import org.junit.Assert;

public class VerifyingExtension implements Extension {

    private ObserverNotifier notifier;
    private FastProcessAnnotatedTypeResolver resolver;
    private RequiredAnnotationDiscovery discovery;
    private ClassTransformer transformer;
    private boolean initialized;
    private ClassFileServices classFileServices;

    void init(@Observes BeforeBeanDiscovery event, BeanManager manager) throws Exception {
        ServiceRegistry services = BeanManagerProxy.unwrap(manager).getServices();
        this.classFileServices = services.get(ClassFileServices.class);
        this.resolver = new FastProcessAnnotatedTypeResolver(
                services.get(GlobalObserverNotifierService.class).getAllObserverMethods());
        this.notifier = BeanManagerProxy.unwrap(manager).getGlobalLenientObserverNotifier();
        this.discovery = services.get(RequiredAnnotationDiscovery.class);
        this.transformer = services.get(ClassTransformer.class);
    }

    void test(@Observes AfterBeanDiscovery event) {
        compareResult(Alpha1Interface.class);
        compareResult(Alpha2Interface.class);
        compareResult(AlphaAbstract.class);
        compareResult(AlphaImpl.class);
        initialized = true;
    }

    private void compareResult(Class<?> javaClass) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Set<?> expected = new HashSet(resolve(javaClass));
        Set<?> actual = resolver.resolveProcessAnnotatedTypeObservers(classFileServices, javaClass.getName());
        if (!expected.equals(actual)) {
            Set<?> notResolved = new HashSet<Object>(expected);
            notResolved.removeAll(actual);
            Set<?> notExpected = new HashSet<Object>(actual);
            notExpected.removeAll(expected);
            Assert.fail("Resolved observer mismatch for " + javaClass + ". Expected but not resolved: " + notResolved
                    + ". Resolved but not expected: " + notExpected);
        }
    }

    private <T> List<ObserverMethod<? super T>> resolve(Class<?> javaClass) {
        Resolvable resolvable = ProcessAnnotatedTypeEventResolvable
                .forProcessAnnotatedType(transformer.getBackedAnnotatedType(javaClass, "foo"), discovery);
        return notifier.<T> resolveObserverMethods(resolvable).getAllObservers();
    }

    public boolean isInitialized() {
        return initialized;
    }
}
