/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.instance.enhanced.selectType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WeldContainer already gives access to WeldInstance - this tests that you can select based on Type
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class WeldInstanceSelectTypeTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(WeldInstanceSelectTypeTest.class))
                        .addPackage(WeldInstanceSelectTypeTest.class.getPackage()))
                .build();
    }

    @Test
    public void testValidSelect() {
        try (WeldContainer container = new Weld().initialize()) {
            // Class<T> implements Type, so let's make use of that
            Type firstType = SomeInterface.class;
            // Let's also grab a parameterized type
            ParameterizedType pType = (ParameterizedType) new TypeLiteral<Foo<Bar>>() {
            }.getType();

            // Instance<Object> -> Instance<SomeInterface>
            WeldInstance<Object> instanceObject = container.select(firstType);
            Object object = instanceObject.get();

            // this can be casted if needed
            Assert.assertTrue(object instanceof SomeInterface);
            SomeInterface si = (SomeInterface) object;
            Assert.assertEquals(SomeOtherBean.class.getSimpleName(), si.ping());

            WeldInstance<Foo<Bar>> parameterizedInstance = container.<Foo<Bar>> select(pType);
            Foo<Bar> fooBar = parameterizedInstance.get();
            Assert.assertNotNull(fooBar);
        }
    }

    @Test
    public void testInvalidSelect() {
        Type secondType = SomeOtherBean.class;
        try (WeldContainer container = new Weld().initialize()) {
            // get more specific instance than Object and try again
            WeldInstance<SomeInterface> someInterfaceInstance = container.select(SomeInterface.class);
            // selecting from anything else than Object is not allowed
            try {
                someInterfaceInstance.select(secondType);
                Assert.fail("Selecting by type from any other type than Object should fail with IllegalStateException!");
            } catch (IllegalStateException e) {
                // expected
            }
        }
    }

    @Test
    public void testSelectOnTypedBean() {
        // typed bean has no Object type, from bean selection perspective, it isn't hierarchical select from WeldInstance<Object>
        Type type = TypedBean.class;
        try (WeldContainer container = new Weld().initialize()) {
            // should work
            TypedBean bean = container.<TypedBean> select(type).get();
            Assert.assertNotNull(bean);
        }
    }

    @Test
    public void testDestroy() {
        Type type = DestroyedBean.class;
        try (WeldContainer container = new Weld().initialize()) {
            WeldInstance<DestroyedBean> instance = container.<DestroyedBean> select(type);
            DestroyedBean bean = instance.get();
            Assert.assertNotNull(bean);
            // destroy and verify
            Assert.assertFalse(DestroyedBean.DESTROY_INVOKED);
            instance.destroy(bean);
            Assert.assertTrue(DestroyedBean.DESTROY_INVOKED);
        }
    }
}
