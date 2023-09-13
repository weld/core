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
package org.jboss.weld.tests.alternatives.weld1286;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that a class with an alternative stereotype may be enabled by listing the bean class within <code>class</code> element
 * of <code>beans.xml</code>. Note that the stereotype is not enabled in <code>beans.xml</code>.
 *
 * @author Jozef Hartinger
 */
@RunWith(Arquillian.class)
public class EnabledClassWithAlternativeStereotypeTest {

    @Inject
    private BeanManager manager;

    @Inject
    private Instance<AlternativeBean> instance;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(EnabledClassWithAlternativeStereotypeTest.class))
                .alternate(AlternativeBean.class)
                .addPackage(EnabledClassWithAlternativeStereotypeTest.class.getPackage());
    }

    @Test
    public void testAlternativeClassEnabled() {
        Bean<?> bean = manager.resolve(manager.getBeans(AlternativeBean.class));
        assertNotNull(bean);
        assertEquals(RequestScoped.class, bean.getScope());
        assertTrue(bean.isAlternative());

        AlternativeBean instance = this.instance.get();
        assertNotNull(instance);
        assertEquals("pong", instance.ping());
    }
}
