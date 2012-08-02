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

package org.jboss.weld.tests.cditck11.interceptors.defitinion.inheritance.resolution.enterprise;

import static org.jboss.weld.util.reflection.Reflections.cast;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Interceptor resolution test.
 *
 * Test taken from org.jboss.cdi.tck.tests.interceptors.definition.inheritance.resolution.enterprise.EnterpriseInterceptorBindingResolutionTest
 * 
 * @author Martin Kouba
 * @author Jozef Hartinger
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class EnterpriseInterceptorBindingResolutionTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class)
                .intercept(ComplicatedInterceptor.class, ComplicatedLifecycleInterceptor.class)
                .addPackage(EnterpriseInterceptorBindingResolutionTest.class.getPackage());
    }

    /**
     * Interceptor bindings include the interceptor bindings declared or inherited by the bean at the class level, including,
     * recursively, interceptor bindings declared as meta-annotations of other interceptor bindings and stereotypes, together
     * with all interceptor bindings declared at the method level, including, recursively, interceptor bindings declared as
     * meta-annotations of other interceptor bindings.
     * 
     * @param messageService
     */
    @SuppressWarnings("serial")
    @Test
    public void testBusinessMethodInterceptorBindings(MessageService messageService, MonitorService monitorService, BeanManager manager) {

        // Test interceptor is resolved (note non-binding member of BallBinding)
        assertEquals(
                manager.resolveInterceptors(InterceptionType.AROUND_INVOKE,
                        new AnnotationLiteral<MessageBinding>() {
                        }, new AnnotationLiteral<LoggedBinding>() {
                        }, new AnnotationLiteral<TransactionalBinding>() {
                        }, new AnnotationLiteral<PingBinding>() {
                        }, new AnnotationLiteral<PongBinding>() {
                        }, new BallBindingLiteral(true, true)).size(), 1);

        // Test the set of interceptor bindings
        assertNotNull(messageService);
        ComplicatedInterceptor.reset();
        messageService.ping();
        assertTrue(ComplicatedInterceptor.intercepted);

        assertNotNull(monitorService);
        ComplicatedInterceptor.reset();
        monitorService.ping();
        assertFalse(ComplicatedInterceptor.intercepted);
    }

    @SuppressWarnings("serial")
    @Test
    public void testLifecycleInterceptorBindings(BeanManager manager) throws Exception {

        // Test interceptor is resolved (note non-binding member of BallBinding)
        assertEquals(
                manager.resolveInterceptors(InterceptionType.POST_CONSTRUCT,
                        new AnnotationLiteral<MessageBinding>() {
                        }, new AnnotationLiteral<LoggedBinding>() {
                        }, new AnnotationLiteral<TransactionalBinding>() {
                        }, new BasketBindingLiteral(true, true)).size(), 1);
        assertEquals(
                manager.resolveInterceptors(InterceptionType.PRE_DESTROY, new AnnotationLiteral<MessageBinding>() {
                }, new AnnotationLiteral<LoggedBinding>() {
                }, new AnnotationLiteral<TransactionalBinding>() {
                }, new BasketBindingLiteral(true, true)).size(), 1);

        // Test the set of interceptor bindings
        ComplicatedLifecycleInterceptor.reset();

        Bean<RemoteMessageService> bean = cast(manager.resolve(manager.getBeans(RemoteMessageService.class)));
        CreationalContext<RemoteMessageService> ctx = manager.createCreationalContext(bean);
        RemoteMessageService remoteMessageService = bean.create(ctx);
        remoteMessageService.ping();
        bean.destroy(remoteMessageService, ctx);

        assertTrue(ComplicatedLifecycleInterceptor.postConstructCalled);
        assertTrue(ComplicatedLifecycleInterceptor.preDestroyCalled);
    }

}
