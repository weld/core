/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.propagation;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test verifies context propagation using an executor service with one thread to which we offload some Callable. Callable is
 * wrapped and automatically handles context activation and propagation. Propagation uses bound contexts.
 *
 * NOTE: Arquillian JMX protocol (default) means that request, session and conversation contexts that will be active will be
 * {@code @Bound} contexts (for instance {@code BoundRequestContextImpl}). Switching this protocol to Servlet will force the use
 * of respective HTTP variant of contexts. To do that, edit {@code wildfly-arquillian.xml} with
 * {@code <defaultProtocol type="Servlet 3.0" />}
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class ContextPropagationTest {

    @Deployment
    public static WebArchive getDeployment() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(ContextPropagationTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(ContextPropagationTest.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    WeldManager manager;

    @Test
    public void testRequestScopedBean() {
        pingBeanAndOffloadTask(manager, ReqScopedBean.class);
    }

    @Test
    public void testApplicationScopedBean() {
        // with app scope, no propagation is needed, it all works out of the box
        pingBeanAndOffloadTask(manager, AppScopedBean.class);
    }

    @Test
    public void testSessionScopedBean() {
        pingBeanAndOffloadTask(manager, SessionScopedBean.class);
    }

    @Test
    public void testConversationScopedBean() {
        pingBeanAndOffloadTask(manager, ConversationScopedBean.class);
    }

    @Test
    public void testSettingEmptyContextualStoreOnCurrentThread() {
        pingBeanAndRunImmediately(manager, ReqScopedBean.class);
    }

    private void pingBeanAndOffloadTask(WeldManager manager, Class<? extends AbstractBeanWithState> beanClazz) {
        // use the bean in this thread first - set counter to one
        AbstractBeanWithState bean = manager.instance().select(beanClazz).get();
        Assert.assertEquals(0, bean.getValue());
        bean.incrementCounter();
        Assert.assertEquals(1, bean.getValue());

        // prepare a callable which will further increase the counter and return the value we gave there
        Callable<Integer> callableTask = () -> {
            // app context is always active and there is no need to propagate it - works out of the box
            AbstractBeanWithState beanInCallable = CDI.current().select(beanClazz).get();
            beanInCallable.incrementCounter();
            return beanInCallable.getValue();
        };
        Future<Integer> futureResult = ContextPropagationService.propagateContextsAndSubmitTask(callableTask);
        // block until we have result
        try {
            Integer result = futureResult.get();
            Assert.assertEquals(2, result.intValue());
        } catch (InterruptedException e) {
            Assert.fail("Encountered InterruptedException while waiting for result from a different thread!");
        } catch (ExecutionException e) {
            Assert.fail(e.toString());
        }
    }

    private void pingBeanAndRunImmediately(WeldManager manager, Class<? extends AbstractBeanWithState> beanClazz) {
        // use the bean in this thread first - set counter to two
        AbstractBeanWithState bean = manager.instance().select(beanClazz).get();
        Assert.assertEquals(0, bean.getValue());
        bean.incrementCounter();
        bean.incrementCounter();
        Assert.assertEquals(2, bean.getValue());

        // prepare a callable which will further increase the counter and return the value we found there
        Callable<Integer> callableTask = () -> {
            AbstractBeanWithState beanInCallable = CDI.current().select(beanClazz).get();
            beanInCallable.incrementCounter();
            return beanInCallable.getValue();
        };
        Integer result = ContextPropagationService.wrapAndRunOnTheSameThread(callableTask);
        Assert.assertEquals(1, result.intValue());
    }
}
