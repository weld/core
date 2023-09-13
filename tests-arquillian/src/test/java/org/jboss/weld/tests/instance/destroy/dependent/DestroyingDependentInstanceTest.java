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
package org.jboss.weld.tests.instance.destroy.dependent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test for CDI-139. It verifies that Instance.destroy() can be used to destroy a dependent bean instance.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class DestroyingDependentInstanceTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DestroyingDependentInstanceTest.class))
                .intercept(Interceptor.class).addPackage(DestroyingDependentInstanceTest.class.getPackage());
    }

    @Test
    public void testDestroyingDependentInstances(Instance<Component> instance) {
        List<Component> createdComponents = new ArrayList<Component>();
        for (int i = 0; i < 10; i++) {
            createdComponents.add(instance.get());
        }
        for (Component component : createdComponents) {
            instance.destroy(component);
        }
        assertEquals(createdComponents, Component.getDestroyedComponents());
    }

    @Test
    public void testDestroyingInterceptedDependentBean(Instance<Intercepted> instance) {
        Intercepted reference = instance.get();
        reference.foo();
        Interceptor.reset();
        instance.destroy(reference);
        assertTrue(Interceptor.isDestroyed());
    }

    @Category(Integration.class)
    @Test // test that destroy doesn't fail with exception
    public void testSLSessionBeanDependentInstanceDestroy() {
        Instance<SLSessionBean> sessionBeanInstance = CDI.current().select(SLSessionBean.class);
        SLSessionBean sessionBean = sessionBeanInstance.get();
        sessionBean.ping();
        sessionBeanInstance.destroy(sessionBean);
    }
}
