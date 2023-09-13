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
package org.jboss.weld.environment.se.test.context.propagation;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.enterprise.context.control.RequestContextController;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Since this test runs in SE, we can only test Application and Request scopes
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class ContextPropagationSETest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class)
                        .addPackage(ContextPropagationSETest.class.getPackage()))
                .build();
    }

    @Test
    public void testRequestScopedBean() {
        try (WeldContainer container = new Weld().initialize()) {
            // in SE we need to controll req context activation/deactivation
            RequestContextController controller = container.select(RequestContextController.class).get();
            controller.activate();

            Future<Integer> futureResult = pingBeanAndOffloadTask(container, ReqScopedBean.class);
            // block until we have result
            try {
                Integer result = futureResult.get();
                controller.deactivate();
                Assert.assertEquals(2, result.intValue());
            } catch (InterruptedException e) {
                Assert.fail("Encountered InterruptedException while waiting for result from a different thread!");
            } catch (ExecutionException e) {
                Assert.fail(e.toString());
            }
        }
    }

    @Test
    public void testApplicationScopedBean() {
        try (WeldContainer container = new Weld().initialize()) {
            // with app scope, no propagation is needed, it all works out of the box
            Future<Integer> futureResult = pingBeanAndOffloadTask(container, AppScopedBean.class);
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
    }

    private Future<Integer> pingBeanAndOffloadTask(WeldContainer container, Class<? extends AbstractBeanWithState> beanClazz) {
        // use the bean in this thread first - set counter to one
        AbstractBeanWithState bean = container.select(beanClazz).get();
        Assert.assertEquals(0, bean.getValue());
        bean.incrementCounter();
        Assert.assertEquals(1, bean.getValue());

        // prepare a callable which will further increase the counter and return the value we gave there
        Callable<Integer> callableTask = () -> {
            WeldContainer weldContainer = WeldContainer.current();
            // app context is always active and there is no need to propagate it - works out of the box
            AbstractBeanWithState beanInCallable = weldContainer.select(beanClazz).get();
            beanInCallable.incrementCounter();
            return beanInCallable.getValue();
        };
        return ContextPropagationSEService.propagateContextsAndSubmitTask(callableTask);
    }
}
