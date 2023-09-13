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
package org.jboss.weld.environment.se.test.weldManager.getContexts;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.context.spi.Context;
import jakarta.inject.Singleton;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.context.WeldAlterableContext;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.weldManager.contextActive.TheLoneBean;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests WeldManager#getActiveContexts() and WeldManager#getActiveWeldAlterableContexts
 *
 * @see WELD-2539
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class GetContextUtilMethodsTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(GetContextUtilMethodsTest.class))
                        .addClasses(GetContextUtilMethodsTest.class, TheLoneBean.class))
                .build();
    }

    @Test
    public void getActiveContextsTest() {
        try (WeldContainer container = new Weld().initialize()) {
            // TheLoneBean is just to have some bean in the archive
            container.select(TheLoneBean.class).get().ping();

            WeldManager manager = container.select(WeldManager.class).get();
            // there are 7 scopes by default in SE, only 3 have active contexts by default
            // it is dependent, singleton and application
            Collection<Context> activeContexts = manager.getActiveContexts();
            Assert.assertEquals(3, activeContexts.size());
            Set<Class<? extends Annotation>> scopes = activeContexts.stream().map(t -> t.getScope())
                    .collect(Collectors.toSet());
            Assert.assertTrue(scopes.contains(Dependent.class));
            Assert.assertTrue(scopes.contains(Singleton.class));
            Assert.assertTrue(scopes.contains(ApplicationScoped.class));
        }
    }

    @Test
    public void getActiveWeldAlterableContextsTest() {
        try (WeldContainer container = new Weld().initialize()) {
            // TheLoneBean is just to have some bean in the archive
            container.select(TheLoneBean.class).get().ping();

            WeldManager manager = container.select(WeldManager.class).get();
            RequestContextController controller = manager.instance().select(RequestContextController.class).get();
            controller.activate();
            Collection<WeldAlterableContext> activeContexts = manager.getActiveWeldAlterableContexts();
            // there are 7 scopes by default in SE, only 3 have active contexts by default
            // it is dependent, singleton and application -> none of these implements WeldAlterableContext
            // therefore we activated request scope and assume on that one
            controller.deactivate();
            Assert.assertEquals(1, activeContexts.size());
            Assert.assertEquals(RequestScoped.class, activeContexts.iterator().next().getScope());
        }
    }
}
