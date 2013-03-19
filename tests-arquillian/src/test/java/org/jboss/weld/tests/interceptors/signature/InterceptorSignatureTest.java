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

package org.jboss.weld.tests.interceptors.signature;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@RunWith(Arquillian.class)
public class InterceptorSignatureTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .intercept(
                        AroundConstructInterceptorWithVoidReturnType.class,
                        AroundConstructInterceptorWithObjectReturnType.class,
                        AroundInvokeInterceptorWithValidSignature.class,
//                        AroundInvokeInterceptorWithInvalidParameterCount.class,
//                        AroundInvokeInterceptorWithInvalidParameterType.class,
//                        AroundInvokeInterceptorWithInvalidReturnType.class,
                        PostConstructInterceptorWithVoidReturnType.class,
                        PostConstructInterceptorWithObjectReturnType.class,
                        PostConstructInterceptorWithInvalidReturnType.class,
                        PostConstructInterceptorWithInvalidParameterCount.class,
                        PostConstructInterceptorWithInvalidParameterType.class)
                .addClasses(
                        TargetClassWithAroundConstruct.class,
                        TargetClassWithAroundInvokeWithInvalidReturnType.class,
                        TargetClassWithAroundInvokeWithInvalidParameterCount.class,
                        TargetClassWithAroundInvokeWithInvalidParameterType.class,
                        AroundConstructInterceptorWithVoidReturnType.class,
                        AroundConstructInterceptorWithObjectReturnType.class,
                        AroundInvokeInterceptorWithValidSignature.class,
//                        AroundInvokeInterceptorWithInvalidParameterCount.class,   // TODO: AS7 EE throws deployment error for invalid AroundInvoke signature
//                        AroundInvokeInterceptorWithInvalidParameterType.class,
//                        AroundInvokeInterceptorWithInvalidReturnType.class,
                        PostConstructInterceptorWithVoidReturnType.class,
                        PostConstructInterceptorWithObjectReturnType.class,
                        PostConstructInterceptorWithInvalidReturnType.class,
                        PostConstructInterceptorWithInvalidParameterCount.class,
                        PostConstructInterceptorWithInvalidParameterType.class,
                        TargetClassWithValidPostConstruct.class,
                        TargetClassWithPostConstructWithInvalidReturnType.class,
                        TargetClassWithPostConstructWithInvalidParameterCount.class,
                        Intercept.class,
                        Lifecycle.class,
                        InterceptedBean.class
                        );
//                .addPackage(InterceptorSignatureTest.class.getPackage());
    }

    @Inject
    private BeanManager beanManager;

    @Test
    public void testTargetClassWithAroundConstruct() {
        TargetClassWithAroundConstruct bean = getBean(TargetClassWithAroundConstruct.class);
        assertNotInvoked(bean.aroundConstructInvoked);
    }

    @Test
    public void testTargetClassWithAroundInvokeWithInvalidReturnType() {
        TargetClassWithAroundInvokeWithInvalidReturnType bean = getBean(TargetClassWithAroundInvokeWithInvalidReturnType.class);
        bean.foo();
        assertNotInvoked(bean.aroundInvokeInvoked);
    }

    @Test
    public void testTargetClassWithAroundInvokeWithInvalidParameterCount() {
        TargetClassWithAroundInvokeWithInvalidParameterCount bean = getBean(TargetClassWithAroundInvokeWithInvalidParameterCount.class);
        bean.foo();
        assertNotInvoked(bean.aroundInvokeInvoked);
    }

    @Test
    public void testTargetClassWithAroundInvokeWithInvalidParameterType() {
        TargetClassWithAroundInvokeWithInvalidParameterType bean = getBean(TargetClassWithAroundInvokeWithInvalidParameterType.class);
        bean.foo();
        assertNotInvoked(bean.aroundInvokeInvoked);
    }

    @Test
    public void testTargetClassLifecycleInterceptor() {
        TargetClassWithValidPostConstruct bean = getBean(TargetClassWithValidPostConstruct.class);
        assertInvoked(bean.postConstructInvoked);
    }

    @Test
    public void testTargetClassLifecycleInterceptorWithInvalidReturnType() {
        TargetClassWithPostConstructWithInvalidReturnType bean = getBean(TargetClassWithPostConstructWithInvalidReturnType.class);
        assertNotInvoked(bean.postConstructInvoked);
    }

    @Test
    public void testTargetClassLifecycleInterceptorWithInvalidParameterCount() {
        TargetClassWithPostConstructWithInvalidParameterCount bean = getBean(TargetClassWithPostConstructWithInvalidParameterCount.class);
        assertNotInvoked(bean.postConstructInvoked);
    }

    @Test
    public void testValidLifecycleInterceptorWithVoidReturnType() {
        PostConstructInterceptorWithVoidReturnType.invoked = false;
        getBean(InterceptedBean.class);
        assertInvoked(PostConstructInterceptorWithVoidReturnType.invoked);
    }

    @Test
    public void testValidLifecycleInterceptorWithObjectReturnType() {
        PostConstructInterceptorWithObjectReturnType.invoked = false;
        getBean(InterceptedBean.class);
        assertInvoked(PostConstructInterceptorWithObjectReturnType.invoked);
    }


    @Test
    public void testLifecycleInterceptorWithInvalidReturnType() {
        PostConstructInterceptorWithInvalidReturnType.invoked = false;
        getBean(InterceptedBean.class);
        assertNotInvoked(PostConstructInterceptorWithInvalidReturnType.invoked);
    }

    @Test
    public void testLifecycleInterceptorWithInvalidParameterCount() {
        PostConstructInterceptorWithInvalidParameterCount.invoked = false;
        getBean(InterceptedBean.class);
        assertNotInvoked(PostConstructInterceptorWithInvalidParameterCount.invoked);
    }

    @Test
    public void testLifecycleInterceptorWithInvalidParameterType() {
        PostConstructInterceptorWithInvalidParameterType.invoked = false;
        getBean(InterceptedBean.class);
        assertNotInvoked(PostConstructInterceptorWithInvalidParameterType.invoked);
    }

    @Test
    public void testAroundConstructInterceptorWithVoidReturnType() {
        AroundConstructInterceptorWithVoidReturnType.invoked= false;
        getBean(InterceptedBean.class);
        assertInvoked(AroundConstructInterceptorWithVoidReturnType.invoked);
    }

    @Test
    public void testAroundConstructInterceptorWithObjectReturnType() {
        AroundConstructInterceptorWithObjectReturnType.invoked = false;
        getBean(InterceptedBean.class);
        assertInvoked(AroundConstructInterceptorWithObjectReturnType.invoked);
    }

    @Test
    public void testAroundInvokeInterceptorWithValidSignature() {
        AroundInvokeInterceptorWithValidSignature.invoked = false;
        InterceptedBean bean = getBean(InterceptedBean.class);
        bean.foo();
        assertInvoked(AroundInvokeInterceptorWithValidSignature.invoked);
    }

//    @Test
//    public void testAroundInvokeInterceptorWithInvalidParameterCount() {
//        AroundInvokeInterceptorWithInvalidParameterCount.invoked = false;
//        getBean(InterceptedBean.class).foo();
//        assertNotInvoked(AroundInvokeInterceptorWithInvalidParameterCount.invoked);
//    }
//
//    @Test
//    public void testAroundInvokeInterceptorWithInvalidParameterType() {
//        AroundInvokeInterceptorWithInvalidParameterType.invoked = false;
//        getBean(InterceptedBean.class).foo();
//        assertNotInvoked(AroundInvokeInterceptorWithInvalidParameterType.invoked);
//    }
//
//    @Test
//    public void testAroundInvokeInterceptorWithInvalidReturnType() {
//        AroundInvokeInterceptorWithInvalidReturnType.invoked = false;
//        getBean(InterceptedBean.class).foo();
//        assertNotInvoked(AroundInvokeInterceptorWithInvalidReturnType.invoked);
//    }

    private <T> T getBean(Class<T> beanType) {
        Set<Bean<?>> beans = beanManager.getBeans(beanType);
        Bean<?> bean = beanManager.resolve(beans);
        CreationalContext<Object> creationalContext = beanManager.createCreationalContext(null);
        try {
            return (T) beanManager.getReference(bean, beanType, creationalContext);
        } finally {
            creationalContext.release();
        }
    }

    private void assertNotInvoked(boolean invoked) {
        assertFalse("interceptor method should not have been invoked, but it was", invoked);
    }

    private void assertInvoked(boolean invoked) {
        assertTrue("interceptor method should have been invoked, but it wasn't", invoked);
    }

}
