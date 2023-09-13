/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.resource;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Unmanaged;
import jakarta.enterprise.inject.spi.Unmanaged.UnmanagedInstance;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Testcase for WELD-1963
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class InterceptorResourceInjectionTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorResourceInjectionTest.class))
                .addPackage(InterceptorResourceInjectionTest.class.getPackage());
    }

    @Test
    public void testWithManagedBean(InterceptedBean bean) {
        Assert.assertNotNull(bean.ping(null));
    }

    @Test
    public void testWithUnmanagedComponent(BeanManager manager) {
        Unmanaged<InterceptedBean> unmanaged = new Unmanaged<>(manager, InterceptedBean.class);
        UnmanagedInstance<InterceptedBean> instance = unmanaged.newInstance();
        InterceptedBean reference = instance.produce().inject().postConstruct().get();
        Assert.assertNotNull(reference.ping(null));
        instance.preDestroy().dispose();
    }
}
