/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.service.additional;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.ServiceLoader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies, that additional {@link Service} implementations are discovered using {@link ServiceLoader} and registered.
 * https://issues.jboss.org/browse/WELD-1495
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class AdditionalServiceTest {

    @Inject
    private BeanManagerImpl manager;

    @Deployment
    public static Archive<?> getDeployment() {
        @SuppressWarnings("rawtypes")
        Class[] classes = new Class[] {
                AlphaImpl.class,
                AlphaService.class,
                Bravo1Service.class,
                Bravo2Service.class,
                BravoImpl.class,
                TransactionServices1.class,
                TransactionServices2.class
        };
        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(AdditionalServiceTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(classes)
                .addAsServiceProvider(Service.class, AlphaImpl.class, BravoImpl.class, TransactionServices1.class,
                        TransactionServices2.class);
    }

    @Test
    public void testSimpleAdditionalService() {
        Assert.assertNotNull(manager.getServices().get(AlphaService.class));
        Assert.assertTrue(manager.getServices().get(AlphaService.class) instanceof AlphaImpl);
        Assert.assertNull(manager.getServices().get(AlphaImpl.class));
    }

    @Test
    public void testAdditionalServiceWithMultipleInterfaces() {
        Bravo1Service bravo1 = manager.getServices().get(Bravo1Service.class);
        Bravo2Service bravo2 = manager.getServices().get(Bravo2Service.class);
        BravoImpl bravo3 = manager.getServices().get(BravoImpl.class);
        Assert.assertNotNull(bravo1);
        Assert.assertNotNull(bravo2);
        Assert.assertNotNull(bravo3);
        Assert.assertTrue(bravo1 == bravo2);
        Assert.assertTrue(bravo2 == bravo3);
    }

    /**
     * description = WFLY-3951
     */
    @Test
    public void testOverridingService() {
        TransactionServices transactionServices = manager.getServices().get(TransactionServices.class);
        Assert.assertNotNull(transactionServices);
        Assert.assertTrue(transactionServices instanceof TransactionServices2);
    }
}
