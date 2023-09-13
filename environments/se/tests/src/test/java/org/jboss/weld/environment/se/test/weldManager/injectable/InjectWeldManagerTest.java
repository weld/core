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
package org.jboss.weld.environment.se.test.weldManager.injectable;

import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that {@link WeldManager} can be injected
 *
 * @see WELD-2538
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class InjectWeldManagerTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InjectWeldManagerTest.class))
                        .addClasses(InjectWeldManagerTest.class, SomeBean.class))
                .build();
    }

    @Test
    public void testWeldManagerInjection() {
        try (WeldContainer container = new Weld().initialize()) {
            WeldInstance<WeldManager> managerInstance = container.select(WeldManager.class);
            Assert.assertTrue(managerInstance.isResolvable());
            // try some call on it
            managerInstance.get().createAnnotatedType(SomeBean.class);

            // verify that BeanManager and BeanManagerImpl are still injectable as well
            WeldInstance<BeanManager> bmInstance = container.select(BeanManager.class);
            Assert.assertTrue(bmInstance.isResolvable());
            WeldInstance<BeanManagerImpl> bmImplInstance = container.select(BeanManagerImpl.class);
            Assert.assertTrue(bmImplInstance.isResolvable());
        }
    }

}
