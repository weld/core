/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.beanManager.serializability;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ManagerTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(ManagerTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(ManagerTest.class.getPackage())
                .addClass(Utils.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private static final Set<Annotation> DEFAULT_QUALIFIERS = Collections.<Annotation> singleton(Default.Literal.INSTANCE);

    private static interface Dummy {
    }

    private static class DummyBean implements Bean<Dummy> {
        private static final Set<Type> TYPES = new HashSet<Type>();

        static {
            TYPES.add(Dummy.class);
            TYPES.add(Object.class);
        }

        public Set<Annotation> getQualifiers() {
            return DEFAULT_QUALIFIERS;
        }

        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

        public String getName() {
            return null;
        }

        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        public Set<Type> getTypes() {
            return TYPES;
        }

        public boolean isNullable() {
            return true;
        }

        public Dummy create(CreationalContext<Dummy> creationalContext) {
            return null;
        }

        public void destroy(Dummy instance, CreationalContext<Dummy> creationalContext) {

        }

        public Class<?> getBeanClass() {
            return Dummy.class;
        }

        public boolean isAlternative() {
            return false;
        }

        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

    }

    @Inject
    private BeanManagerImpl beanManager;

    @Test
    public void testRootManagerSerializability() throws Exception {
        String rootManagerId = beanManager.getId();
        BeanManagerImpl deserializedRootManager = (BeanManagerImpl) Utils.deserialize(Utils.serialize(beanManager));
        Assert.assertEquals(rootManagerId, deserializedRootManager.getId());
        Assert.assertEquals(1, beanManager.getBeans(Foo.class).size());
        Assert.assertEquals(1, deserializedRootManager.getBeans(Foo.class).size());
        Assert.assertEquals(
                deserializedRootManager.getBeans(Foo.class).iterator().next(),
                beanManager.getBeans(Foo.class).iterator().next());
    }
}
