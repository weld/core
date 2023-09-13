/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.builtinBeans.ee;

import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManager;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkEntityManagerFactory;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkRemoteEjb;
import static org.jboss.weld.tests.builtinBeans.ee.Checker.checkUserTransaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class EEResourceProducerFieldPassivationCapableTest {
    @Deployment // changed to .war, from .jar
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(WebArchive.class,
                        Utils.getDeploymentNameAsHash(EEResourceProducerFieldPassivationCapableTest.class,
                                Utils.ARCHIVE_TYPE.WAR))
                .addPackage(EEResourceProducerFieldPassivationCapableTest.class.getPackage())
                .addClass(Utils.class)
                .addAsResource(
                        EEResourceProducerFieldPassivationCapableTest.class.getPackage(),
                        "persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @Ignore("Will work once we start testing with WFLY 11+, as there will be fix for JBTM-2449")
    public void testResource(@Produced UserTransaction userTransaction) throws Throwable {
        // invoke deserialization with special class loader as the underlying class will not be otherwise visible to Weld
        UserTransaction userTransaction1 = Utils.deserialize(Utils.serialize(userTransaction),
                userTransaction.getClass().getClassLoader());
        Assert.assertTrue(checkUserTransaction(userTransaction1));
    }

    @Test
    public void testEntityManager(@Produced EntityManager entityManager) throws Throwable {
        EntityManager entityManager1 = Utils.deserialize(Utils.serialize(entityManager));
        Assert.assertTrue(checkEntityManager(entityManager1));
    }

    @Test
    public void testEntityManagerFactory(@Produced EntityManagerFactory entityManagerFactory) throws Throwable {
        EntityManagerFactory entityManagerFactory1 = Utils.deserialize(Utils.serialize(entityManagerFactory));
        Assert.assertTrue(checkEntityManagerFactory(entityManagerFactory1));
    }

    @Test
    public void testRemoteEjb(@Produced HorseRemote horse) throws Throwable {
        HorseRemote horse1 = Utils.deserialize(Utils.serialize(horse));
        Assert.assertTrue(checkRemoteEjb(horse1));
    }

    @Test
    public void testAllOnBean(EEResourceConsumer consumer) throws Throwable {
        consumer.check();
        EEResourceConsumer consumer1 = Utils.deserialize(Utils.serialize(consumer));
        consumer1.check();
    }

}
