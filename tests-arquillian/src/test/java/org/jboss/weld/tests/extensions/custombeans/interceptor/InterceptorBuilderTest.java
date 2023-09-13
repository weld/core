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
package org.jboss.weld.tests.extensions.custombeans.interceptor;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bootstrap.events.BuilderInterceptorInstance;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InterceptorBuilderTest {

    @Inject
    Foo foo;

    @Inject
    BuilderExtension extension;

    @Inject
    BeanManager bm;

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorBuilderTest.class))
                .addPackage(InterceptorBuilderTest.class.getPackage())
                .addAsServiceProvider(Extension.class, BuilderExtension.class).addClass(Utils.class);
    }

    @Test
    public void testTypeLevelInterceptorFromBuilder() {
        foo.ping();
        Assert.assertNotNull(extension.getInjectedBean());
        Assert.assertEquals(Foo.class, extension.getInjectedBean().getBeanClass());
        Assert.assertTrue(extension.getInterceptedWithMetadata().get());
        List<Interceptor<?>> interceptorList = bm.resolveInterceptors(InterceptionType.AROUND_INVOKE,
                TypeBinding.TypeBindingLiteral.INSTANCE);
        Assert.assertEquals(1, interceptorList.size());
        Assert.assertEquals(BuilderInterceptorInstance.class, interceptorList.get(0).getBeanClass());
    }

    @Test
    public void testMethodLevelInterceptorFromBuilder() {
        foo.methodLevel();
        Assert.assertTrue(extension.getIntercepted().get());
        List<Interceptor<?>> interceptorList = bm.resolveInterceptors(InterceptionType.AROUND_INVOKE,
                MethodBinding.MethodBindingLiteral.INSTANCE);
        Assert.assertEquals(1, interceptorList.size());
        Assert.assertEquals(BuilderInterceptorInstance.class, interceptorList.get(0).getBeanClass());
    }

    @Test
    public void testBuilderInterceptorInstanceIsSerializable() throws Exception {
        Set<Bean<?>> beans = bm.getBeans(Bar.class);
        Assert.assertFalse(beans.isEmpty());
        Bean<?> bean = beans.iterator().next();
        CreationalContext<?> ctx = bm.createCreationalContext(bean);
        Bar instance = (Bar) bm.getReference(bean, Bar.class, ctx);
        Assert.assertEquals(1, instance.ping());
        Bar passivatedBar = (Bar) Utils.deserialize(Utils.serialize(bm.getContext(SessionScoped.class).get(bean)));
        Assert.assertEquals(2, passivatedBar.ping());
    }

}
