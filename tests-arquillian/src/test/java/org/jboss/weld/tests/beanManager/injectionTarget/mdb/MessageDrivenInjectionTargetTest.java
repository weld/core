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
package org.jboss.weld.tests.beanManager.injectionTarget.mdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.construction.api.WeldCreationalContext;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see https://issues.jboss.org/browse/WELD-1345
 * @see https://issues.jboss.org/browse/WELD-2404
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class MessageDrivenInjectionTargetTest {

    @Inject
    private BeanManagerImpl manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(MessageDrivenInjectionTargetTest.class))
                .addPackage(MessageDrivenInjectionTargetTest.class.getPackage());
    }

    @Test
    public void testInjectionTargetCreatedForMessageDrivenBean() {
        InjectionTarget<MessageDriven> it = manager.createInjectionTarget(createEjbDescriptor());

        CreationalContext<MessageDriven> ctx = manager.<MessageDriven> createCreationalContext(null);
        AroundInvokeInterceptor.reset();

        MessageDriven instance = it.produce(ctx);
        assertFalse(instance.isPostConstruct());
        assertFalse(instance.isDestroyed());
        assertNull(instance.getField());
        assertNull(instance.getInitializer());
        assertNotNull(instance.getConstructor());
        assertTrue(AroundConstructInterceptor.aroundConstructCalled);

        it.inject(instance, ctx);
        assertFalse(instance.isPostConstruct());
        assertFalse(instance.isDestroyed());
        assertNotNull(instance.getField());
        assertNotNull(instance.getInitializer());
        assertNotNull(instance.getConstructor());

        it.postConstruct(instance);
        assertTrue(instance.isPostConstruct());
        assertFalse(instance.isDestroyed());
        assertNotNull(instance.getField());
        assertNotNull(instance.getInitializer());
        assertNotNull(instance.getConstructor());

        instance.ping();
        assertEquals(1, AroundInvokeInterceptor.interceptedInvocations.size());
        assertTrue(AroundInvokeInterceptor.interceptedInvocations.contains("ping"));

        it.preDestroy(instance);
        assertTrue(instance.isPostConstruct());
        assertTrue(instance.isDestroyed());
        assertNotNull(instance.getField());
        assertNotNull(instance.getInitializer());
        assertNotNull(instance.getConstructor());

        it.dispose(instance);
        ctx.release();
    }

    @Test
    public void testInjectionTargetCreatedForMessageDrivenBeanWithNoAroundConstruct() {
        InjectionTarget<MessageDriven> it = manager.createInjectionTarget(createEjbDescriptor());

        CreationalContext<MessageDriven> ctx = manager.<MessageDriven> createCreationalContext(null);
        // use Weld CC to disable around construct
        WeldCreationalContext<MessageDriven> weldCC = (WeldCreationalContext<MessageDriven>) ctx;
        weldCC.setConstructorInterceptionSuppressed(true);

        AroundInvokeInterceptor.reset();

        MessageDriven instance = it.produce(ctx);
        assertFalse(instance.isPostConstruct());
        assertFalse(instance.isDestroyed());
        assertNull(instance.getField());
        assertNull(instance.getInitializer());
        assertNotNull(instance.getConstructor());
        // @AroundConstruct should NOT be invoked here
        assertFalse(AroundConstructInterceptor.aroundConstructCalled);

        it.dispose(instance);
        weldCC.release();
    }

    private EjbDescriptor<MessageDriven> createEjbDescriptor() {
        return new EjbDescriptor<MessageDriven>() {

            @Override
            public Class<MessageDriven> getBeanClass() {
                return MessageDriven.class;
            }

            @Override
            public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces() {
                return null;
            }

            @Override
            public Collection<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces() {
                return null;
            }

            @Override
            public String getEjbName() {
                return null;
            }

            @Override
            public Collection<Method> getRemoveMethods() {
                return null;
            }

            @Override
            public boolean isStateless() {
                return false;
            }

            @Override
            public boolean isSingleton() {
                return false;
            }

            @Override
            public boolean isStateful() {
                return false;
            }

            @Override
            public boolean isMessageDriven() {
                return true;
            }

            @Override
            public boolean isPassivationCapable() {
                return false;
            }
        };
    }
}
