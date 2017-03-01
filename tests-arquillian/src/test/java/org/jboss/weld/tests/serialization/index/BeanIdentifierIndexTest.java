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
package org.jboss.weld.tests.serialization.index;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.web.HttpSessionBean;
import org.jboss.weld.serialization.BeanIdentifierIndex;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.serialization.noncontextual.SerializationTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class BeanIdentifierIndexTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(SerializationTest.class))
                .addPackage(BeanIdentifierIndexTest.class.getPackage());
    }

    @Inject
    BeanManagerImpl beanManager;

    @Test
    public void testIndex() {
        BeanIdentifierIndex index = beanManager.getServices().get(BeanIdentifierIndex.class);
        // Index is enabled by default
        assertTrue(index.isBuilt());
        assertFalse(index.isEmpty());
        ManagedBean<?> indexedBean = (ManagedBean<?>) beanManager.resolve(beanManager.getBeans(Indexed.class));
        assertNotNull(index.getIndex(indexedBean.getIdentifier()));
        HttpSessionBean httpSessionBean = (HttpSessionBean) beanManager.resolve(beanManager.getBeans(HttpSession.class));
        // Built-in beans are not included
        assertNull(index.getIndex(httpSessionBean.getIdentifier()));
    }

}
