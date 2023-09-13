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
package org.jboss.weld.tests.injectionPoint.resource.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Matus Abaffy
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class PersistenceInjectionTest {

    public static final Asset PERSISTENCE_XML = new ByteArrayAsset(
            "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\" version=\"2.0\"><persistence-unit name=\"test\"><jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source></persistence-unit></persistence>"
                    .getBytes());

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(PersistenceInjectionTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addPackage(PersistenceInjectionTest.class.getPackage())
                .addClass(ActionSequence.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(PERSISTENCE_XML, "META-INF/persistence.xml");
    }

    @Test
    public void testResourceInjection(Alpha alpha) {
        assertNotNull(alpha);
        assertNotNull(alpha.persistenceContext);
        assertNotNull(alpha.persistenceUnit);
        assertNotNull(alpha.superPersistenceContext);
        assertNotNull(alpha.superPersistenceUnit);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResourceInjectionOrdering(BeanManager beanManager) {
        ActionSequence.reset();

        Bean<Alpha> bean = (Bean<Alpha>) beanManager.resolve(beanManager.getBeans(Alpha.class));
        CreationalContext<Alpha> ctx = beanManager.createCreationalContext(bean);
        Alpha instance = bean.create(ctx);
        bean.destroy(instance, ctx);

        ActionSequence sequence = ActionSequence.getSequence();
        assertEquals(4, sequence.getData().size());
        assertTrue(sequence.beginsWith(Charlie.class.getName() + EntityManager.class.getName(), Bravo.class.getName()
                + EntityManagerFactory.class.getName()));
    }

}
