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
package org.jboss.weld.tests.cditck11.lookup.manager;


import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.literal.DefaultLiteral;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Taken from org.jboss.cdi.tck.tests.lookup.manager.ManagerTest
 *
 */
@RunWith(Arquillian.class)
public class ManagerTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(ManagerTest.class.getPackage());
    }

    @Test
    public void testInjectingManager(FishFarmOffice fishFarmOffice) {
        assert fishFarmOffice.beanManager != null;
    }

    @Test
    public void testContainerProvidesManagerBean(BeanManager manager) {
        assert manager.getBeans(BeanManager.class).size() > 0;
    }

    @Test
    public void testManagerBeanIsDependentScoped(BeanManager manager) {
        Bean<BeanManager> beanManager = cast(manager.getBeans(BeanManager.class).iterator().next());
        assert beanManager.getScope().equals(Dependent.class);
    }

    @Test
    public void testManagerBeanHasCurrentBinding(BeanManager manager) {
        Bean<BeanManager> beanManager = cast(manager.getBeans(BeanManager.class).iterator().next());
        assert beanManager.getQualifiers().contains(DefaultLiteral.INSTANCE);
    }

    @Test
    public void testManagerBeanIsPassivationCapable(BeanManager manager) {
        assert isSerializable(manager.getClass());
    }

    @Test
    public void testGetReferenceReturnsContextualInstance(BeanManager manager) {
        Bean<FishFarmOffice> bean = cast(manager.getBeans(FishFarmOffice.class).iterator().next());
        assert manager.getReference(bean, FishFarmOffice.class, manager.createCreationalContext(bean)) instanceof FishFarmOffice;
    }

    @Test(expected = IllegalArgumentException.class )
    public void testGetReferenceWithIllegalBeanType(BeanManager manager) {
        Bean<FishFarmOffice> bean = cast(manager.getBeans(FishFarmOffice.class).iterator().next());
        manager.getReference(bean, BigDecimal.class, manager.createCreationalContext(bean));
    }

    private boolean isSerializable(Class<?> clazz) {
        return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
    }
}
