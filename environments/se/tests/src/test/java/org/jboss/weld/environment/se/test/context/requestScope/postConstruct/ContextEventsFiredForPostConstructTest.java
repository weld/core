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
package org.jboss.weld.environment.se.test.context.requestScope.postConstruct;

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
 * @author Tomas Remes
 */
@RunWith(Arquillian.class)
public class ContextEventsFiredForPostConstructTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class)
                        .addClasses(ContextEventsFiredForPostConstructTest.class, ContextLifecycleObserver.class, Alpha.class,
                                Beta.class, Gama.class))
                .build();
    }

    @Test
    public void contextLifecycleEventFiredForPostConstructCallbackActivation() {
        try (WeldContainer container = new Weld().initialize()) {
            Alpha alpha = container.select(Alpha.class).get();
            alpha.ping();
            ContextLifecycleObserver ctxLifecycleObserver = container.select(ContextLifecycleObserver.class).get();
            Assert.assertEquals(3, ctxLifecycleObserver.getInitCount().get());
            Assert.assertEquals(3, ctxLifecycleObserver.getBeforeDestroyedCount().get());
            Assert.assertEquals(3, ctxLifecycleObserver.getDestroyedCount().get());

        }
    }
}
